@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)

package io.github.smiley4.schemakenerator.serialization.steps

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.CollectionTypeData
import io.github.smiley4.schemakenerator.core.data.EnumTypeData
import io.github.smiley4.schemakenerator.core.data.MapTypeData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PlaceholderTypeData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.PropertyType
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeParameterData
import io.github.smiley4.schemakenerator.core.data.Visibility
import io.github.smiley4.schemakenerator.core.data.WildcardTypeData
import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.serializerOrNull
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaField

/**
 * Processes the given type and extracts information about it using kotlinx-serialization. Types must be serializable, i.e. annotated with [Serializable]
 */
class KotlinxSerializationTypeProcessingStep(
    /**
     * custom processors for given types that overwrite the default behaviour
     */
    private val customProcessors: Map<String, () -> BaseTypeData> = emptyMap(),
    /**
     * redirect types to other types, i.e. when a type is found as a key, the corresponding type will be processed instead
     */
    private val typeRedirects: Map<String, KType> = emptyMap(),

    /**
     * types that are known to not have any type parameters
     */
    private val knownNotParameterized: Set<String> = emptySet()

) {

    fun process(type: KType): Bundle<BaseTypeData> = process(Bundle(type, emptyList()))

    fun process(type: Bundle<KType>): Bundle<BaseTypeData> {
        val supportingTypeData = mutableListOf<BaseTypeData>()
        type.supporting.forEach { process(it, supportingTypeData) }

        val typeData = process(type.data, supportingTypeData)
        supportingTypeData.remove(typeData.typeData)

        return Bundle(
            data = typeData.typeData,
            supporting = supportingTypeData
        )
    }

    private fun process(type: KType, typeData: MutableList<BaseTypeData>): WrappedTypeData {
        if (type.classifier is KClass<*>) {
            return (type.classifier as KClass<*>).serializerOrNull()
                ?.let { parse(it.descriptor, type.isMarkedNullable, typeData, mutableMapOf()) }
                ?: parseWildcard(typeData)
        } else {
            throw IllegalArgumentException("Type is not a class.")
        }
    }


    @Suppress("CyclomaticComplexMethod")
    private fun parse(
        descriptor: SerialDescriptor,
        nullable: Boolean,
        typeData: MutableList<BaseTypeData>,
        processed: MutableMap<SerialDescriptor, BaseTypeData>
    ): WrappedTypeData {

        // break out of infinite loops
        if (processed.containsKey(descriptor)) {
            return WrappedTypeData(typeData = processed[descriptor]!!, nullable = nullable)
        }
        processed[descriptor] = PlaceholderTypeData(TypeId.wildcard())

        // check redirects
        if (typeRedirects.containsKey(descriptor.redirectKey(nullable))) {
            return process(typeRedirects[descriptor.redirectKey(nullable)]!!, typeData).also {
                processed[descriptor] = it.typeData
            }
        }

        // check custom processors
        if (customProcessors.containsKey(descriptor.cleanSerialName())) {
            return customProcessors[descriptor.cleanSerialName()]!!.invoke()
                .also { result ->
                    typeData.removeIf { it.id == result.id }
                    typeData.add(result)
                }
                .also { processed[descriptor] = it }
                .let { WrappedTypeData(typeData = it, nullable = false) }
        }

        // process
        return when (descriptor.cleanSerialName()) {
            Unit::class.qualifiedName -> parsePrimitive(descriptor, typeData)
            UByte::class.qualifiedName -> parsePrimitive(descriptor, typeData)
            UShort::class.qualifiedName -> parsePrimitive(descriptor, typeData)
            UInt::class.qualifiedName -> parsePrimitive(descriptor, typeData)
            ULong::class.qualifiedName -> parsePrimitive(descriptor, typeData)
            else -> when (descriptor.kind) {
                StructureKind.LIST -> parseList(descriptor, typeData, processed)
                StructureKind.MAP -> parseMap(descriptor, typeData, processed)
                StructureKind.CLASS -> parseClass(descriptor, typeData, processed)
                StructureKind.OBJECT -> parseObject(descriptor, typeData)
                PolymorphicKind.OPEN -> parseSealed(descriptor, typeData, processed)
                PolymorphicKind.SEALED -> parseSealed(descriptor, typeData, processed)
                PrimitiveKind.BOOLEAN -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.BYTE -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.CHAR -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.DOUBLE -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.FLOAT -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.INT -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.LONG -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.SHORT -> parsePrimitive(descriptor, typeData)
                PrimitiveKind.STRING -> parsePrimitive(descriptor, typeData)
                SerialKind.ENUM -> parseEnum(descriptor, typeData)
                SerialKind.CONTEXTUAL -> parseClass(descriptor, typeData, processed)
            }
        }
            .let { WrappedTypeData(typeData = it, nullable = nullable || descriptor.isNullable) }
            .also { processed[descriptor] = it.typeData }
    }

    private fun parseWildcard(typeData: MutableList<BaseTypeData>): WrappedTypeData {
        val wildcard = WildcardTypeData()
        val type = typeData.find(wildcard.id) ?: wildcard.also { typeData.add(it) }
        return WrappedTypeData(
            typeData = type,
            nullable = false
        )
    }

    private fun parsePrimitive(descriptor: SerialDescriptor, typeData: MutableList<BaseTypeData>): BaseTypeData {
        val id = TypeId.build(descriptor.cleanSerialName())
        return typeData.find(id)
            ?: PrimitiveTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                annotations = parseAnnotations(descriptor)
            ).also { result ->
                typeData.removeIf { it.id == result.id }
                typeData.add(result)
            }
    }

    private fun parseList(
        descriptor: SerialDescriptor,
        typeData: MutableList<BaseTypeData>,
        processed: MutableMap<SerialDescriptor, BaseTypeData>
    ): BaseTypeData {
        val itemDescriptor = descriptor.getElementDescriptor(0)
        val itemName = descriptor.getElementName(0)
        val itemType = parse(itemDescriptor, false, typeData, processed)
        val id = TypeId.build(descriptor.cleanSerialName(), listOf(itemType.typeData.id))
        return typeData.find(id)
            ?: CollectionTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                typeParameters = mutableMapOf(
                    itemName to TypeParameterData(
                        name = itemName,
                        type = itemType.typeData.id,
                        nullable = itemDescriptor.isNullable || itemType.nullable,
                    )
                ),
                itemType = PropertyData(
                    name = "item",
                    type = itemType.typeData.id,
                    nullable = itemDescriptor.isNullable || itemType.nullable,
                    optional = false,
                    kind = PropertyType.PROPERTY,
                    visibility = Visibility.PUBLIC,
                ),
                unique = false,
                annotations = parseAnnotations(descriptor)
            ).also { result ->
                typeData.removeIf { it.id == result.id }
                typeData.add(result)
            }
    }

    private fun parseMap(
        descriptor: SerialDescriptor,
        typeData: MutableList<BaseTypeData>,
        processed: MutableMap<SerialDescriptor, BaseTypeData>
    ): BaseTypeData {
        val keyDescriptor = descriptor.getElementDescriptor(0)
        val keyName = descriptor.getElementName(0)
        val keyType = parse(keyDescriptor, false, typeData, processed)

        val valueDescriptor = descriptor.getElementDescriptor(1)
        val valueName = descriptor.getElementName(1)
        val valueType = parse(valueDescriptor, false, typeData, processed)

        val id = TypeId.build(descriptor.cleanSerialName(), listOf(keyType.typeData.id, valueType.typeData.id))
        return typeData.find(id)
            ?: MapTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                typeParameters = mutableMapOf(
                    keyName to TypeParameterData(
                        name = keyName,
                        type = keyType.typeData.id,
                        nullable = keyDescriptor.isNullable || keyType.nullable,
                    ),
                    valueName to TypeParameterData(
                        name = valueName,
                        type = valueType.typeData.id,
                        nullable = valueDescriptor.isNullable || valueType.nullable,
                    )
                ),
                keyType = PropertyData(
                    name = "key",
                    type = keyType.typeData.id,
                    nullable = keyDescriptor.isNullable || keyType.nullable,
                    optional = false,
                    kind = PropertyType.PROPERTY,
                    visibility = Visibility.PUBLIC,

                    ),
                valueType = PropertyData(
                    name = "value",
                    type = valueType.typeData.id,
                    nullable = valueDescriptor.isNullable || valueType.nullable,
                    optional = false,
                    kind = PropertyType.PROPERTY,
                    visibility = Visibility.PUBLIC,
                ),
                annotations = parseAnnotations(descriptor)
            ).also { result ->
                typeData.removeIf { it.id == result.id }
                typeData.add(result)
            }
    }

    private fun parseClass(
        descriptor: SerialDescriptor,
        typeData: MutableList<BaseTypeData>,
        processed: MutableMap<SerialDescriptor, BaseTypeData>
    ): BaseTypeData {
        val id = getUniqueId(descriptor, emptyList(), typeData) // unique for each object since generic types cannot be respected in id
        return typeData.find(id)
            ?: ObjectTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                members = buildList {
                    for (i in 0..<descriptor.elementsCount) {
                        val fieldDescriptor = descriptor.getElementDescriptor(i)
                        val fieldName = descriptor.getElementName(i)
                        val fieldType = parse(fieldDescriptor, false, typeData, processed)
                        add(
                            PropertyData(
                                name = fieldName,
                                type = fieldType.typeData.id,
                                nullable = fieldDescriptor.isNullable || fieldType.nullable,
                                optional = descriptor.isElementOptional(i),
                                kind = PropertyType.PROPERTY,
                                visibility = Visibility.PUBLIC,
                                annotations = parseAnnotations(descriptor.getElementAnnotations(i)),
                            )
                        )
                    }
                }.toMutableList(),
                isInlineValue = descriptor.isInline,
                annotations = parseAnnotations(descriptor)
            ).also { result ->
                typeData.removeIf { it.id == result.id }
                typeData.add(result)
            }
    }

    private fun parseSealed(
        descriptor: SerialDescriptor,
        typeData: MutableList<BaseTypeData>,
        processed: MutableMap<SerialDescriptor, BaseTypeData>
    ): BaseTypeData {
        val id = getUniqueId(descriptor, emptyList(), typeData) // unique for each object since generic types cannot be respected in id
        return typeData.find(id)
            ?: ObjectTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                subtypes = descriptor.elementDescriptors
                    .toList()[1].elementDescriptors
                    .map { parse(it, false, typeData, processed).typeData.id }
                    .toMutableList(),
                annotations = parseAnnotations(descriptor)
            ).also { result ->
                typeData.removeIf { it.id == result.id }
                typeData.add(result)
            }
    }


    private fun parseObject(descriptor: SerialDescriptor, typeData: MutableList<BaseTypeData>): BaseTypeData {
        val id = TypeId.build(descriptor.cleanSerialName())
        return typeData.find(id)
            ?: ObjectTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                annotations = parseAnnotations(descriptor)
            ).also { result ->
                typeData.removeIf { it.id == result.id }
                typeData.add(result)
            }
    }


    private fun parseEnum(descriptor: SerialDescriptor, typeData: MutableList<BaseTypeData>): BaseTypeData {
        val id = TypeId.build(descriptor.cleanSerialName())
        return typeData.find(id)
            ?: EnumTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                enumConstants = descriptor.elementNames.toMutableList(),
                annotations = parseAnnotations(descriptor)
            ).also { result ->
                typeData.removeIf { it.id == result.id }
                typeData.add(result)
            }
    }

    // ====== ANNOTATION ===============================================

    private fun parseAnnotations(descriptor: SerialDescriptor): MutableList<AnnotationData> {
        return parseAnnotations(descriptor.annotations)
    }

    private fun parseAnnotations(annotations: List<Annotation>): MutableList<AnnotationData> {
        return unwrapAnnotations(annotations).map { parseAnnotation(it) }.toMutableList()
    }

    private fun unwrapAnnotations(annotations: List<Annotation>): List<Annotation> {
        // "repeatable" annotations are wrapped in a container class and need to be unwrapped
        return annotations.flatMap { annotation ->
            if (isAnnotationContainer(annotation)) {
                unwrapContainer(annotation)
            } else {
                listOf(annotation)
            }
        }
    }

    private fun isAnnotationContainer(annotation: Annotation): Boolean {
        return annotation.annotationClass.java.declaredAnnotations
            .map { it.annotationClass.qualifiedName }
            .contains("kotlin.jvm.internal.RepeatableContainer")
    }


    @Suppress("SwallowedException")
    private fun unwrapContainer(annotation: Annotation): List<Annotation> {
        try {
            // A repeatable annotation container must have a method "value" returning the array of repeated annotations.
            val valueMethod = annotation.javaClass.getMethod("value")
            @Suppress("UNCHECKED_CAST")
            return (valueMethod(annotation) as Array<Annotation>).asList()
        } catch (e: Exception) {
            return emptyList()
        }
    }

    private fun parseAnnotation(annotation: Annotation): AnnotationData {
        return AnnotationData(
            name = annotation.annotationClass.qualifiedName ?: "",
            annotation = annotation,
            values = annotation.annotationClass.members
                .filterIsInstance<KProperty<*>>()
                .filter { it.javaField?.let { jf -> !Modifier.isStatic(jf.modifiers) } ?: true }
                .associate { it.name to it.getter.call(annotation) }
                .toMutableMap()
        )
    }

    // ====== UTILITIES ================================================

    private fun getUniqueId(descriptor: SerialDescriptor, typeParameters: List<TypeId>, typeData: MutableList<BaseTypeData>): TypeId {
        if (knownNotParameterized.contains(descriptor.cleanSerialName())) {
            return TypeId.build(descriptor.cleanSerialName(), typeParameters)
        }
        if (typeData.find(descriptor, typeParameters) != null) {
            return TypeId.build(descriptor.cleanSerialName(), typeParameters, true)
        }
        return TypeId.build(descriptor.cleanSerialName(), typeParameters)
    }

    private fun Collection<BaseTypeData>.find(descriptor: SerialDescriptor, typeParameters: List<TypeId>): BaseTypeData? {
        val typeId = TypeId.build(descriptor.cleanSerialName(), typeParameters)
        return this.find { it.id.full() == typeId.full() }
    }

    private fun Collection<BaseTypeData>.find(id: TypeId): BaseTypeData? {
        return this.find { it.id.full() == id.full() }
    }

    private fun SerialDescriptor.cleanSerialName() = this.serialName.replace("?", "")

    private fun SerialDescriptor.redirectKey(nullable: Boolean) = cleanSerialName() + if (nullable || this.isNullable) "?" else ""


    @OptIn(ExperimentalSerializationApi::class)
    fun SerialDescriptor.qualifiedName() = this.serialName.replace("?", "")


    @OptIn(ExperimentalSerializationApi::class)
    fun SerialDescriptor.simpleName() = this.serialName.split(".").last().replace("?", "")

}
