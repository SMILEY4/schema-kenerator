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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.capturedKClass
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.modules.SerializersModule
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
     * kotlinx serializers module from `Json { }.serializersModule` for support of contextual serializers
     */
    private val serializersModule: SerializersModule? = null,
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
        supportingTypeData.remove(typeData)

        return Bundle(
            data = typeData,
            supporting = supportingTypeData
        )
    }

    private fun process(type: KType, typeData: MutableList<BaseTypeData>): BaseTypeData {
        if (type.classifier is KClass<*>) {
            return (type.classifier as KClass<*>).serializerOrNull()
                ?.let { parse(it.descriptor, typeData, mutableMapOf()) }
                ?: parseWildcard(typeData)
        } else {
            throw IllegalArgumentException("Type is not a class.")
        }
    }


    @Suppress("CyclomaticComplexMethod")
    private fun parse(
        descriptor: SerialDescriptor,
        typeData: MutableList<BaseTypeData>,
        processed: MutableMap<SerialDescriptor, BaseTypeData>
    ): BaseTypeData {

        // break out of infinite loops
        if (processed.containsKey(descriptor)) {
            return processed[descriptor]!!
        }
        processed[descriptor] = PlaceholderTypeData(TypeId.wildcard())

        // check redirects
        if (typeRedirects.containsKey(descriptor.cleanSerialName())) {
            return process(typeRedirects[descriptor.cleanSerialName()]!!, typeData).also {
                processed[descriptor] = it
            }
        }

        // check custom processors
        if (customProcessors.containsKey(descriptor.cleanSerialName())) {
            return customProcessors[descriptor.cleanSerialName()]!!.invoke()
                .also { result ->
                    typeData.removeIf { it.id == result.id }
                    typeData.add(result)
                }
                .also {
                    processed[descriptor] = it
                }
        }

        // check contextual descriptors
        val contextualByKClass = descriptor.capturedKClass?.let { serializersModule!!.getContextual(it)?.descriptor }
        if(contextualByKClass != null) {
            return parse(contextualByKClass, typeData, processed)
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
        }.also {
            processed[descriptor] = it
        }
    }

    private fun parseWildcard(typeData: MutableList<BaseTypeData>): BaseTypeData {
        val type = WildcardTypeData()
        return typeData.find(type.id) ?: type.also { typeData.add(it) }
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
        val itemType = parse(itemDescriptor, typeData, processed)
        val id = TypeId.build(descriptor.cleanSerialName(), listOf(itemType.id))
        return typeData.find(id)
            ?: CollectionTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                typeParameters = mutableMapOf(
                    itemName to TypeParameterData(
                        name = itemName,
                        type = itemType.id,
                        nullable = itemDescriptor.isNullable,
                    )
                ),
                itemType = PropertyData(
                    name = "item",
                    type = itemType.id,
                    nullable = itemDescriptor.isNullable,
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
        val keyType = parse(keyDescriptor, typeData, processed)

        val valueDescriptor = descriptor.getElementDescriptor(1)
        val valueName = descriptor.getElementName(1)
        val valueType = parse(valueDescriptor, typeData, processed)

        val id = TypeId.build(descriptor.cleanSerialName(), listOf(keyType.id, valueType.id))
        return typeData.find(id)
            ?: MapTypeData(
                id = id,
                simpleName = descriptor.simpleName(),
                qualifiedName = descriptor.qualifiedName(),
                typeParameters = mutableMapOf(
                    keyName to TypeParameterData(
                        name = keyName,
                        type = keyType.id,
                        nullable = keyDescriptor.isNullable,
                    ),
                    valueName to TypeParameterData(
                        name = valueName,
                        type = valueType.id,
                        nullable = valueDescriptor.isNullable,
                    )
                ),
                keyType = PropertyData(
                    name = "key",
                    type = keyType.id,
                    nullable = keyDescriptor.isNullable,
                    optional = false,
                    kind = PropertyType.PROPERTY,
                    visibility = Visibility.PUBLIC,

                    ),
                valueType = PropertyData(
                    name = "value",
                    type = valueType.id,
                    nullable = valueDescriptor.isNullable,
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
                        val fieldType = parse(fieldDescriptor, typeData, processed)
                        add(
                            PropertyData(
                                name = fieldName,
                                type = fieldType.id,
                                nullable = fieldDescriptor.isNullable,
                                optional = descriptor.isElementOptional(i),
                                kind = PropertyType.PROPERTY,
                                visibility = Visibility.PUBLIC,
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
                    .map { parse(it, typeData, processed).id }
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

    private fun parseAnnotations(descriptor: SerialDescriptor): MutableList<AnnotationData>{
        return unwrapAnnotations(descriptor.annotations).map { parseAnnotation(it) }.toMutableList()
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
        if(knownNotParameterized.contains(descriptor.cleanSerialName())) {
            return TypeId.build(descriptor.cleanSerialName(), typeParameters)
        }
        if(typeData.find(descriptor, typeParameters) != null) {
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


    @OptIn(ExperimentalSerializationApi::class)
    fun SerialDescriptor.qualifiedName() = this.serialName.replace("?", "")


    @OptIn(ExperimentalSerializationApi::class)
    fun SerialDescriptor.simpleName() = this.serialName.split(".").last().replace("?", "")

}
