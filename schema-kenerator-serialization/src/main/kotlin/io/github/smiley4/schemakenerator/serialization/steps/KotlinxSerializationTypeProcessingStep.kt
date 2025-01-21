@file:OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class, ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.serialization.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.typedata.AnnotationData
import io.github.smiley4.schemakenerator.core.typedata.CollectionData
import io.github.smiley4.schemakenerator.core.typedata.EnumData
import io.github.smiley4.schemakenerator.core.typedata.MapData
import io.github.smiley4.schemakenerator.core.typedata.MemberData
import io.github.smiley4.schemakenerator.core.typedata.MemberKind
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.core.typedata.TypeName
import io.github.smiley4.schemakenerator.core.typedata.TypeParameterData
import io.github.smiley4.schemakenerator.core.typedata.Visibility
import io.github.smiley4.schemakenerator.core.typedata.WrappedTypeData
import io.github.smiley4.schemakenerator.core.typedata.matches
import io.github.smiley4.schemakenerator.serialization.SerialDescriptorInput
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
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
    private val customProcessors: List<Pair<KotlinxSerializationTypeMatcher, KotlinxSerializationCustomProcessor>> = emptyList(),
    /**
     * redirect types to other types, i.e. when a type is found as a key, the corresponding type will be processed instead
     */
    private val typeRedirects: Map<String, InputType> = emptyMap(),

    /**
     * types that are known to not have any type parameters
     */
    private val knownNotParameterized: Set<String> = emptySet()

) {

    /**
     * Process the given input type
     */
    fun process(input: InputType): Bundle<TypeData> = process(Bundle(input, emptyList()))


    /**
     * Process the given input type bundle
     */
    fun process(input: Bundle<InputType>): Bundle<TypeData> {

        val knownTypeData = mutableListOf<TypeData>()

        // process supporting inputs
        input.supporting.forEach {
            process(it, knownTypeData)
        }

        // process main input
        val typeData = process(input.data, knownTypeData)

        knownTypeData.remove(typeData.typeData)
        return Bundle(
            data = typeData.typeData,
            supporting = knownTypeData
        )
    }

    private fun process(input: InputType, knownTypeData: MutableList<TypeData>): WrappedTypeData {
        return when (input) {
            is KTypeInput -> getSerializer(input.kType)
                ?.let { serializer -> parse(serializer.descriptor, input.kType.isMarkedNullable, knownTypeData, mutableMapOf()) }
                ?: WrappedTypeData(
                    typeData = knownTypeData.find(TypeId.createWildcard()) ?: TypeData.createWildcard(),
                    nullable = false
                )
            is SerialDescriptorInput -> parse(input.descriptor, input.descriptor.isNullable, knownTypeData, mutableMapOf())
            else -> throw IllegalArgumentException("Unsupported input type '$input'.")
        }
    }


    /**
     * Get the serializer (or null) for the given type
     */
    private fun getSerializer(type: KType): KSerializer<Any?>? {
        return if (type.classifier is KClass<*>) {
            try {
                serializerOrNull(type)
            } catch (ignore: IllegalArgumentException) {
                null
            }
        } else {
            throw IllegalArgumentException("Type '$type' is not a class.")
        }
    }


    /**
     * Parses the given descriptor and adds the results to the given collection
     * @param descriptor the input descriptor to parse
     * @param nullable whether the input descriptor is (externally) marked as nullable
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @param processedDescriptors already processed descriptors with their type data. Adds new results to this map.
     */
    @Suppress("CyclomaticComplexMethod")
    private fun parse(
        descriptor: SerialDescriptor,
        nullable: Boolean,
        knownTypeData: MutableList<TypeData>,
        processedDescriptors: MutableMap<SerialDescriptor, TypeData>
    ): WrappedTypeData {

        // input serial descriptor has already been parsed before (or is currently being parsed) -> break out of infinite loops
        if (processedDescriptors.containsKey(descriptor)) {
            return WrappedTypeData(typeData = processedDescriptors[descriptor]!!, nullable = nullable)
        }

        // reserve this descriptor / mark this descriptor as processed with a pending result
        processedDescriptors[descriptor] = TypeData.createWildcard()

        // check type redirects
        if (typeRedirects.containsKey(descriptor.redirectKey(nullable))) {
            val redirectTo = typeRedirects[descriptor.redirectKey(nullable)]!!
            return process(redirectTo, knownTypeData).also {
                processedDescriptors[descriptor] = it.typeData
            }
        }

        // check custom processors
        val customData = customProcessors
            .firstOrNull { (matcher, _) -> matcher(descriptor) }
            ?.let { (_, processor) ->
                WrappedTypeData(
                    typeData = processor(),
                    nullable = false
                )
            }
            ?.also { result ->
                knownTypeData.removeIf {
                    it.matches(
                        other = result.typeData,
                        compareId = false,
                        compareIdentifyingName = true,
                        compareDescriptiveName = true,
                        compareTypeParameters = true
                    )
                }
                knownTypeData.add(result.typeData)
                processedDescriptors[descriptor] = result.typeData
            }
        if (customData != null) {
            return customData
        }

        // parse descriptor
        return when (descriptor.fullName()) {
            Unit::class.qualifiedName -> parsePrimitive(descriptor, knownTypeData, Unit::class.toTypeName())
            UByte::class.qualifiedName -> parsePrimitive(descriptor, knownTypeData, UByte::class.toTypeName())
            UShort::class.qualifiedName -> parsePrimitive(descriptor, knownTypeData, UShort::class.toTypeName())
            UInt::class.qualifiedName -> parsePrimitive(descriptor, knownTypeData, UInt::class.toTypeName())
            ULong::class.qualifiedName -> parsePrimitive(descriptor, knownTypeData, ULong::class.toTypeName())
            else -> when (descriptor.kind) {
                StructureKind.LIST -> parseList(descriptor, knownTypeData, processedDescriptors)
                StructureKind.MAP -> parseMap(descriptor, knownTypeData, processedDescriptors)
                StructureKind.CLASS -> parseClass(descriptor, knownTypeData, processedDescriptors)
                StructureKind.OBJECT -> parseObject(descriptor, knownTypeData)
                PolymorphicKind.OPEN -> parseSealed(descriptor, knownTypeData, processedDescriptors)
                PolymorphicKind.SEALED -> parseSealed(descriptor, knownTypeData, processedDescriptors)
                PrimitiveKind.BOOLEAN -> parsePrimitive(descriptor, knownTypeData, Boolean::class.toTypeName())
                PrimitiveKind.BYTE -> parsePrimitive(descriptor, knownTypeData, Byte::class.toTypeName())
                PrimitiveKind.CHAR -> parsePrimitive(descriptor, knownTypeData, Char::class.toTypeName())
                PrimitiveKind.DOUBLE -> parsePrimitive(descriptor, knownTypeData, Double::class.toTypeName())
                PrimitiveKind.FLOAT -> parsePrimitive(descriptor, knownTypeData, Float::class.toTypeName())
                PrimitiveKind.INT -> parsePrimitive(descriptor, knownTypeData, Int::class.toTypeName())
                PrimitiveKind.LONG -> parsePrimitive(descriptor, knownTypeData, Long::class.toTypeName())
                PrimitiveKind.SHORT -> parsePrimitive(descriptor, knownTypeData, Short::class.toTypeName())
                PrimitiveKind.STRING -> parsePrimitive(descriptor, knownTypeData, String::class.toTypeName())
                SerialKind.ENUM -> parseEnum(descriptor, knownTypeData)
                SerialKind.CONTEXTUAL -> parseClass(descriptor, knownTypeData, processedDescriptors)
            }
        }
            .also { result ->
                knownTypeData.removeIf {
                    it.matches(
                        other = result,
                        compareId = false,
                        compareIdentifyingName = true,
                        compareDescriptiveName = true,
                        compareTypeParameters = true
                    )
                }
                knownTypeData.add(result)
            }
            .let {
                WrappedTypeData(
                    typeData = it,
                    nullable = nullable || descriptor.isNullable
                )
            }
            .also { processedDescriptors[descriptor] = it.typeData }
    }


    /**
     * Parse the given serial descriptor classified as a primitive type
     * @param descriptor the serial descriptor to parse
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @param identifyingName the identifying name for this type. Might be different as the name from the serial descriptor
     */
    private fun parsePrimitive(descriptor: SerialDescriptor, knownTypeData: MutableList<TypeData>, identifyingName: TypeName): TypeData {

        // create basic information for this type
        val descriptiveName = descriptor.toTypeName()

        // check type has already been parsed
        val existing = knownTypeData.find { known -> known.matches(identifyingName, descriptiveName, emptyList()) }
        if (existing != null) {
            return existing
        }

        // collect annotation data
        val annotations = parseAnnotations(descriptor)

        // build type
        return TypeData(
            id = TypeId.create(),
            identifyingName = identifyingName,
            descriptiveName = descriptiveName,
            typeParameters = mutableListOf(),
            annotations = annotations,
            subtypes = mutableListOf(),
            supertypes = mutableListOf(),
            members = mutableListOf(),
            isInlineValue = false,
            enumData = null,
            collectionData = null,
            mapData = null
        )
    }


    /**
     * Parse the given serial descriptor classified as a list
     * @param descriptor the serial descriptor to parse
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @param processedDescriptors already processed descriptors with their type data. Adds new results to this map.
     */
    private fun parseList(
        descriptor: SerialDescriptor,
        knownTypeData: MutableList<TypeData>,
        processedDescriptors: MutableMap<SerialDescriptor, TypeData>
    ): TypeData {

        // create basic information for this type
        val descriptiveName = descriptor.toTypeName()
        val identifyingName = descriptor.toTypeName()

        // collect information about item type
        val itemDescriptor = descriptor.getElementDescriptor(0)
        val itemName = descriptor.getElementName(0)
        val itemType = parse(itemDescriptor, false, knownTypeData, processedDescriptors)
        val itemParameter = TypeParameterData(
            name = itemName,
            type = itemType.typeData.id,
            nullable = itemDescriptor.isNullable || itemType.nullable,
        )

        // check type has already been parsed
        val existing = knownTypeData.find { known -> known.matches(identifyingName, descriptiveName, listOf(itemParameter)) }
        if (existing != null) {
            return existing
        }

        // collect annotation data
        val annotations = parseAnnotations(descriptor)

        // build type
        return TypeData(
            id = TypeId.create(),
            identifyingName = identifyingName,
            descriptiveName = descriptiveName,
            typeParameters = mutableListOf(itemParameter),
            annotations = annotations,
            subtypes = mutableListOf(),
            supertypes = mutableListOf(),
            members = mutableListOf(),
            isInlineValue = false,
            enumData = null,
            collectionData = CollectionData(
                unique = false,
                itemType = MemberData(
                    name = "item",
                    type = itemParameter.type,
                    nullable = itemParameter.nullable,
                    optional = false,
                    kind = MemberKind.PROPERTY,
                    visibility = Visibility.PUBLIC,
                    annotations = mutableListOf(),
                ),
            ),
            mapData = null
        )
    }


    /**
     * Parse the given serial descriptor classified as a map
     * @param descriptor the serial descriptor to parse
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @param processedDescriptors already processed descriptors with their type data. Adds new results to this map.
     */
    private fun parseMap(
        descriptor: SerialDescriptor,
        knownTypeData: MutableList<TypeData>,
        processedDescriptors: MutableMap<SerialDescriptor, TypeData>
    ): TypeData {

        // create basic information for this type
        val descriptiveName = descriptor.toTypeName()
        val identifyingName = descriptor.toTypeName()

        // collect information about key type
        val keyDescriptor = descriptor.getElementDescriptor(0)
        val keyName = descriptor.getElementName(0)
        val keyType = parse(keyDescriptor, false, knownTypeData, processedDescriptors)
        val keyParameter = TypeParameterData(
            name = keyName,
            type = keyType.typeData.id,
            nullable = keyDescriptor.isNullable || keyType.nullable,
        )

        // collect information about value type
        val valueDescriptor = descriptor.getElementDescriptor(1)
        val valueName = descriptor.getElementName(1)
        val valueType = parse(valueDescriptor, false, knownTypeData, processedDescriptors)
        val valueParameter = TypeParameterData(
            name = valueName,
            type = valueType.typeData.id,
            nullable = valueDescriptor.isNullable || valueType.nullable,
        )

        // check type has already been parsed
        val existing = knownTypeData.find { known -> known.matches(identifyingName, descriptiveName, listOf(keyParameter, valueParameter)) }
        if (existing != null) {
            return existing
        }

        // collect annotation data
        val annotations = parseAnnotations(descriptor)

        // build type
        return TypeData(
            id = TypeId.create(),
            identifyingName = identifyingName,
            descriptiveName = descriptiveName,
            typeParameters = mutableListOf(keyParameter, valueParameter),
            annotations = annotations,
            subtypes = mutableListOf(),
            supertypes = mutableListOf(),
            members = mutableListOf(),
            isInlineValue = false,
            enumData = null,
            collectionData = null,
            mapData = MapData(
                keyType = MemberData(
                    name = "key",
                    type = keyParameter.type,
                    nullable = keyParameter.nullable,
                    optional = false,
                    kind = MemberKind.PROPERTY,
                    visibility = Visibility.PUBLIC,
                    annotations = mutableListOf(),
                ),
                valueType = MemberData(
                    name = "value",
                    type = valueParameter.type,
                    nullable = valueParameter.nullable,
                    optional = false,
                    kind = MemberKind.PROPERTY,
                    visibility = Visibility.PUBLIC,
                    annotations = mutableListOf(),
                ),
            )
        )
    }


    /**
     * Parse the given serial descriptor classified as a class
     * @param descriptor the serial descriptor to parse
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @param processedDescriptors already processed descriptors with their type data. Adds new results to this map.
     */
    private fun parseClass(
        descriptor: SerialDescriptor,
        knownTypeData: MutableList<TypeData>,
        processedDescriptors: MutableMap<SerialDescriptor, TypeData>
    ): TypeData {

        // create basic information for this type
        val descriptiveName = descriptor.toTypeName()
        val identifyingName = descriptor.toTypeName()

        // Check type has already been parsed.
        // Only search if we know this type does not have type parameters, otherwise we can't trust possible matches due to
        // possible differences in type parameters that are not exposed by kotlinx-serialization.
        if (knownNotParameterized.contains(descriptiveName.full)) {
            val existing = knownTypeData.find { known -> known.matches(identifyingName, descriptiveName, listOf()) }
            if (existing != null) {
                return existing
            }
        }

        // collect annotation data
        val annotations = parseAnnotations(descriptor)

        // collect members
        val members = buildList {
            for (i in 0..<descriptor.elementsCount) {
                val fieldName = descriptor.getElementName(i)
                val fieldDescriptor = descriptor.getElementDescriptor(i)
                val fieldType = parse(fieldDescriptor, false, knownTypeData, processedDescriptors)
                add(
                    MemberData(
                        name = fieldName,
                        type = fieldType.typeData.id,
                        nullable = fieldDescriptor.isNullable || fieldType.nullable,
                        optional = descriptor.isElementOptional(i),
                        kind = MemberKind.PROPERTY,
                        visibility = Visibility.PUBLIC,
                        annotations = parseAnnotations(descriptor.getElementAnnotations(i)),
                    )
                )
            }
        }

        // whether class is inline class
        val isInline = descriptor.isInline

        // build type
        return TypeData(
            id = TypeId.create(),
            identifyingName = identifyingName,
            descriptiveName = descriptiveName,
            typeParameters = mutableListOf(),
            annotations = annotations,
            subtypes = mutableListOf(),
            supertypes = mutableListOf(),
            members = members.toMutableList(),
            isInlineValue = isInline,
            enumData = null,
            collectionData = null,
            mapData = null
        )
    }


    /**
     * Parse the given serial descriptor classified as a sealed class
     * @param descriptor the serial descriptor to parse
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @param processedDescriptors already processed descriptors with their type data. Adds new results to this map.
     */
    private fun parseSealed(
        descriptor: SerialDescriptor,
        knownTypeData: MutableList<TypeData>,
        processedDescriptors: MutableMap<SerialDescriptor, TypeData>
    ): TypeData {

        // create basic information for this type
        val descriptiveName = descriptor.toTypeName()
        val identifyingName = descriptor.toTypeName()

        // Check type has already been parsed.
        // Only search if we know this type does not have type parameters, otherwise we can't trust possible matches due to
        // possible differences in type parameters that are not exposed by kotlinx-serialization.
        if (knownNotParameterized.contains(descriptiveName.full)) {
            val existing = knownTypeData.find { known -> known.matches(identifyingName, descriptiveName, listOf()) }
            if (existing != null) {
                return existing
            }
        }

        // collect annotation data
        val annotations = parseAnnotations(descriptor)

        // collect subtypes
        val subtypes = descriptor.elementDescriptors
            .toList()[1].elementDescriptors
            .map { parse(it, false, knownTypeData, processedDescriptors).typeData.id }

        // build type
        return TypeData(
            id = TypeId.create(),
            identifyingName = identifyingName,
            descriptiveName = descriptiveName,
            typeParameters = mutableListOf(),
            annotations = annotations,
            subtypes = subtypes.toMutableList(),
            supertypes = mutableListOf(),
            members = mutableListOf(),
            isInlineValue = false,
            enumData = null,
            collectionData = null,
            mapData = null
        )
    }


    /**
     * Parse the given serial descriptor classified as an object
     * @param descriptor the serial descriptor to parse
     * @param knownTypeData the already known type data. Adds new results to this collection.
     */
    private fun parseObject(
        descriptor: SerialDescriptor,
        knownTypeData: MutableList<TypeData>,
    ): TypeData {

        // create basic information for this type
        val descriptiveName = descriptor.toTypeName()
        val identifyingName = descriptor.toTypeName()

        // check type has already been parsed
        val existing = knownTypeData.find { known -> known.matches(identifyingName, descriptiveName, listOf()) }
        if (existing != null) {
            return existing
        }

        // collect annotation data
        val annotations = parseAnnotations(descriptor)

        // build type
        return TypeData(
            id = TypeId.create(),
            identifyingName = identifyingName,
            descriptiveName = descriptiveName,
            typeParameters = mutableListOf(),
            annotations = annotations,
            subtypes = mutableListOf(),
            supertypes = mutableListOf(),
            members = mutableListOf(),
            isInlineValue = false,
            enumData = null,
            collectionData = null,
            mapData = null
        )
    }


    /**
     * Parse the given serial descriptor classified as an enum
     * @param descriptor the serial descriptor to parse
     * @param knownTypeData the already known type data. Adds new results to this collection.
     */
    private fun parseEnum(
        descriptor: SerialDescriptor,
        knownTypeData: MutableList<TypeData>,
    ): TypeData {

        // create basic information for this type
        val descriptiveName = descriptor.toTypeName()
        val identifyingName = descriptor.toTypeName()

        // check type has already been parsed
        val existing = knownTypeData.find { known -> known.matches(identifyingName, descriptiveName, listOf()) }
        if (existing != null) {
            return existing
        }

        // collect annotation data
        val annotations = parseAnnotations(descriptor)

        // collect enum constants
        val constants = descriptor.elementNames

        // build type
        return TypeData(
            id = TypeId.create(),
            identifyingName = identifyingName,
            descriptiveName = descriptiveName,
            typeParameters = mutableListOf(),
            annotations = annotations,
            subtypes = mutableListOf(),
            supertypes = mutableListOf(),
            members = mutableListOf(),
            isInlineValue = false,
            enumData = EnumData(
                constants = constants.toMutableList()
            ),
            collectionData = null,
            mapData = null
        )
    }

    // ====== ANNOTATION ===============================================

    /**
     * Parses the annotations on the given serial descriptor.
     * @return the list of parsed annotation data
     */
    private fun parseAnnotations(descriptor: SerialDescriptor): MutableList<AnnotationData> {
        return parseAnnotations(descriptor.annotations)
    }


    /**
     * Parses the given list of annotations.
     * @return the list of parsed annotation data
     */
    private fun parseAnnotations(annotations: List<Annotation>): MutableList<AnnotationData> {
        return unwrapAnnotations(annotations).map { parseAnnotation(it) }.toMutableList()
    }


    /**
     * Unwrap any "container" (i.e. "repeatable") annotation in the given list of annotations.
     * "repeatable" annotations are wrapped in a container class and need to be unwrapped
     * @return the flat list of all annotations. Each instance of a "repeatable" annotation is an item in the list.
     */
    private fun unwrapAnnotations(annotations: List<Annotation>): List<Annotation> {
        return annotations.flatMap { annotation ->
            if (isAnnotationContainer(annotation)) {
                unwrapContainer(annotation)
            } else {
                listOf(annotation)
            }
        }
    }


    /**
     * @return whether the given annotation is a "container" annotation containing multiple "repeatable" annotation of the same type.
     */
    private fun isAnnotationContainer(annotation: Annotation): Boolean {
        return annotation.annotationClass.java.declaredAnnotations
            .map { it.annotationClass.qualifiedName }
            .contains("kotlin.jvm.internal.RepeatableContainer")
    }


    /**
     * Unwraps the given container annotation containing multiple "repeatable" annotations of the same type.
     * @return each contained annotation as an own item in the list
     */
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


    /**
     * Parses the given annotation
     * @return the result as [AnnotationData]
     */
    private fun parseAnnotation(annotation: Annotation): AnnotationData {
        return AnnotationData(
            name = annotation.annotationClass.getSafeQualifiedName(), // todo: did not use "getSafeQualifiedName" before -> check if this was an error
            values = annotation.annotationClass.members
                .filterIsInstance<KProperty<*>>()
                .filter { it.javaField?.let { jf -> !Modifier.isStatic(jf.modifiers) } ?: true }
                .associate { it.name to it.getter.call(annotation) }
                .toMutableMap()
        )
    }

    // ====== UTILITIES ================================================

    /**
     * @return the type data with the given id or null
     */
    private fun Collection<TypeData>.find(id: TypeId): TypeData? = this.find { it.id == id }


    /**
     * @return a [TypeName] for this class
     */
    private fun KClass<*>.toTypeName() = TypeName(
        full = this.getSafeQualifiedName(),
        short = this.simpleName ?: this.java.name
    )


    /**
     * Qualified name might be null, e.g. for local classes
     */
    private fun KClass<*>.getSafeQualifiedName(): String = this.qualifiedName ?: this.java.name


    /**
     * @return a [TypeName] for this class
     */
    private fun SerialDescriptor.toTypeName() = TypeName(
        full = this.fullName(),
        short = this.shortName(),
    )

    /**
     * @return a key used to match redirect types
     */
    private fun SerialDescriptor.redirectKey(nullable: Boolean) = fullName() + if (nullable || this.isNullable) "?" else ""

}
