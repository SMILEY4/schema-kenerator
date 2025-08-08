@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.data.CollectionData
import io.github.smiley4.schemakenerator.core.data.EnumData
import io.github.smiley4.schemakenerator.core.data.MapData
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.MemberKind
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataUtils.matches
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName
import io.github.smiley4.schemakenerator.core.data.TypeParameterData
import io.github.smiley4.schemakenerator.core.data.Visibility
import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.elementNames
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class DefaultSerializationTypeAnalyzerModule(
    /**
     * Whether to find type parameters using reflection whenever possible
     */
    private val findTypeParametersUsingReflection: Boolean
) : SerializationTypeAnalyzerModule {

    private val annotationAnalyzer = AnnotationAnalyzer()

    override fun applies(descriptor: SerialDescriptor) = true


    @Suppress("CyclomaticComplexMethod")
    override fun analyze(context: SerializationTypeAnalyzerModule.Context): WrappedTypeData {
        return when (context.descriptor.fullName()) {
            Unit::class.qualifiedName -> analyzePrimitive(context, Unit::class.toTypeName())
            UByte::class.qualifiedName -> analyzePrimitive(context, UByte::class.toTypeName())
            UShort::class.qualifiedName -> analyzePrimitive(context, UShort::class.toTypeName())
            UInt::class.qualifiedName -> analyzePrimitive(context, UInt::class.toTypeName())
            ULong::class.qualifiedName -> analyzePrimitive(context, ULong::class.toTypeName())
            else -> when (context.descriptor.kind) {
                StructureKind.LIST -> analyzeList(context)
                StructureKind.MAP -> analyzeMap(context)
                StructureKind.CLASS -> analyzeClass(context)
                StructureKind.OBJECT -> analyzeObject(context)
                PolymorphicKind.OPEN -> analyzeSealed(context)
                PolymorphicKind.SEALED -> analyzeSealed(context)
                PrimitiveKind.BOOLEAN -> analyzePrimitive(context, Boolean::class.toTypeName())
                PrimitiveKind.BYTE -> analyzePrimitive(context, Byte::class.toTypeName())
                PrimitiveKind.CHAR -> analyzePrimitive(context, Char::class.toTypeName())
                PrimitiveKind.DOUBLE -> analyzePrimitive(context, Double::class.toTypeName())
                PrimitiveKind.FLOAT -> analyzePrimitive(context, Float::class.toTypeName())
                PrimitiveKind.INT -> analyzePrimitive(context, Int::class.toTypeName())
                PrimitiveKind.LONG -> analyzePrimitive(context, Long::class.toTypeName())
                PrimitiveKind.SHORT -> analyzePrimitive(context, Short::class.toTypeName())
                PrimitiveKind.STRING -> analyzePrimitive(context, String::class.toTypeName())
                SerialKind.ENUM -> analyzeEnum(context)
                SerialKind.CONTEXTUAL -> analyzeClass(context)
            }
        }.let {
            WrappedTypeData(
                typeData = it,
                nullable = context.descriptor.isNullable
            )
        }
    }


    /**
     * Analyze the given serial descriptor classified as a primitive type
     * @param context the context with the current type to analyze
     * @param identifyingName the identifying name for this type. Might be different as the name from the serial descriptor
     */
    private fun analyzePrimitive(context: SerializationTypeAnalyzerModule.Context, identifyingName: TypeName): TypeData {

        // create basic information for this type
        val descriptiveName = context.toTypeName()

        // check type has already been parsed
        val existing = context.findKnown(identifyingName, descriptiveName)
        if (existing != null) {
            return existing
        }

        // collect annotation data
        val annotations = annotationAnalyzer.analyzeAnnotations(context.descriptor)

        // build type
        return TypeData(
            id = context.id,
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
     * Analyze the given serial descriptor classified as a list
     * @param context the context with the current type to analyze
     */
    private fun analyzeList(context: SerializationTypeAnalyzerModule.Context): TypeData {

        // create basic information for this type
        val name = context.toTypeName()

        // collect information about item type
        val itemDescriptor = context.descriptor.getElementDescriptor(0)
        val itemName = context.descriptor.getElementName(0)
        val itemType = context.analyze(itemDescriptor)
        val itemParameter = TypeParameterData(
            name = itemName,
            type = itemType.typeData.id,
            nullable = itemDescriptor.isNullable || itemType.nullable,
        )

        // check type has already been parsed
        val existing = context.findKnown(typeParameters = listOf(itemParameter))
        if (existing != null) {
            return existing
        }

        // collect annotation data
        val annotations = annotationAnalyzer.analyzeAnnotations(context.descriptor)

        // check if list or set
        val uniqueItems = context.descriptor.javaClass.name == "kotlinx.serialization.internal.LinkedHashSetClassDesc"
                || context.descriptor.javaClass.name == "kotlinx.serialization.internal.HashSetClassDesc"

        // build type
        return TypeData(
            id = context.id,
            identifyingName = name,
            descriptiveName = name,
            typeParameters = mutableListOf(itemParameter),
            annotations = annotations,
            subtypes = mutableListOf(),
            supertypes = mutableListOf(),
            members = mutableListOf(),
            isInlineValue = false,
            enumData = null,
            collectionData = CollectionData(
                unique = uniqueItems,
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
     * Analyze the given serial descriptor classified as a map
     * @param context the context with the current type to analyze
     */
    private fun analyzeMap(context: SerializationTypeAnalyzerModule.Context): TypeData {

        // create basic information for this type
        val name = context.toTypeName()

        // collect information about key type
        val keyDescriptor = context.descriptor.getElementDescriptor(0)
        val keyName = context.descriptor.getElementName(0)
        val keyType = context.analyze(keyDescriptor)
        val keyParameter = TypeParameterData(
            name = keyName,
            type = keyType.typeData.id,
            nullable = keyDescriptor.isNullable || keyType.nullable,
        )

        // collect information about value type
        val valueDescriptor = context.descriptor.getElementDescriptor(1)
        val valueName = context.descriptor.getElementName(1)
        val valueType = context.analyze(valueDescriptor)
        val valueParameter = TypeParameterData(
            name = valueName,
            type = valueType.typeData.id,
            nullable = valueDescriptor.isNullable || valueType.nullable,
        )

        // check type has already been parsed
        val existing = context.findKnown(typeParameters = listOf(keyParameter, valueParameter))
        if (existing != null) {
            return existing
        }

        // collect annotation data
        val annotations = annotationAnalyzer.analyzeAnnotations(context.descriptor)

        // build type
        return TypeData(
            id = context.id,
            identifyingName = name,
            descriptiveName = name,
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
     * Analyze the given serial descriptor classified as a class
     * @param context the context with the current type to analyze
     */
    private fun analyzeClass(context: SerializationTypeAnalyzerModule.Context): TypeData {

        // create basic information for this type
        val name = context.toTypeName()

        // extract type parameters using reflection (if enabled)
        val typeParameters = if (findTypeParametersUsingReflection) {
            extractTypeParameters(context.descriptor).mapIndexed { index, descriptor ->
                val type = context.analyze(descriptor)
                TypeParameterData(
                    name = "T$index",
                    type = type.typeData.id,
                    nullable = type.nullable,
                )
            }
        } else {
            emptyList()
        }

        // Check type has already been parsed.
        val existing = context.findKnown(typeParameters = typeParameters)
        if (existing != null) {
            return existing
        }

        // collect annotation data
        val annotations = annotationAnalyzer.analyzeAnnotations(context.descriptor)

        // collect members
        val members = buildList {
            for (i in 0..<context.descriptor.elementsCount) {
                val fieldName = context.descriptor.getElementName(i)
                val fieldDescriptor = context.descriptor.getElementDescriptor(i)
                val fieldType = context.analyze(fieldDescriptor)
                add(
                    MemberData(
                        name = fieldName,
                        type = fieldType.typeData.id,
                        nullable = fieldDescriptor.isNullable || fieldType.nullable,
                        optional = context.descriptor.isElementOptional(i),
                        kind = MemberKind.PROPERTY,
                        visibility = Visibility.PUBLIC,
                        annotations = annotationAnalyzer.analyzeAnnotations(context.descriptor.getElementAnnotations(i)),
                    )
                )
            }
        }

        // whether class is inline class
        val isInline = context.descriptor.isInline

        // build type
        return TypeData(
            id = context.id,
            identifyingName = name,
            descriptiveName = name,
            typeParameters = typeParameters.toMutableList(),
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
     * Analyze the given serial descriptor classified as a sealed class
     * @param context the context with the current type to analyze
     */
    private fun analyzeSealed(context: SerializationTypeAnalyzerModule.Context): TypeData {

        // create basic information for this type
        val name = context.toTypeName()

        // extract type parameters using reflection (if enabled)
        val typeParameters = if (findTypeParametersUsingReflection) {
            extractTypeParameters(context.descriptor).mapIndexed { index, descriptor ->
                val type = context.analyze(descriptor)
                TypeParameterData(
                    name = "T$index",
                    type = type.typeData.id,
                    nullable = type.nullable,
                )
            }
        } else {
            emptyList()
        }

        // Check type has already been parsed.
        val existing = context.findKnown(typeParameters = typeParameters)
        if (existing != null) {
            return existing
        }

        // collect annotation data
        val annotations = annotationAnalyzer.analyzeAnnotations(context.descriptor)

        // collect subtypes
        val subtypes = context.descriptor.elementDescriptors
            .toList()[1].elementDescriptors
            .map { context.analyze(it).typeData.id }

        // build type
        return TypeData(
            id = context.id,
            identifyingName = name,
            descriptiveName = name,
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
     * Analyze the given serial descriptor classified as an object
     * @param context the context with the current type to analyze
     */
    private fun analyzeObject(context: SerializationTypeAnalyzerModule.Context): TypeData {

        // create basic information for this type
        val name = context.toTypeName()

        // check type has already been parsed
        val existing = context.findKnown()
        if (existing != null) {
            return existing
        }

        // collect annotation data
        val annotations = annotationAnalyzer.analyzeAnnotations(context.descriptor)

        // build type
        return TypeData(
            id = context.id,
            identifyingName = name,
            descriptiveName = name,
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
     * Analyze the given serial descriptor classified as an enum
     * @param context the context with the current type to analyze
     */
    private fun analyzeEnum(context: SerializationTypeAnalyzerModule.Context): TypeData {

        // create basic information for this type
        val name = context.toTypeName()

        // check type has already been parsed
        val existing = context.findKnown()
        if (existing != null) {
            return existing
        }

        // collect annotation data
        val annotations = annotationAnalyzer.analyzeAnnotations(context.descriptor)

        // collect enum constants
        val constants = context.descriptor.elementNames

        // build type
        return TypeData(
            id = TypeId.create(),
            identifyingName = name,
            descriptiveName = name,
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


    /**
     * Try to determine the type parameters of the given serial descriptor (if possible).
     */
    private fun extractTypeParameters(serialDescriptor: SerialDescriptor): List<SerialDescriptor> {
        if (serialDescriptor::class.qualifiedName == "kotlinx.serialization.internal.PluginGeneratedSerialDescriptor") {
            val property = serialDescriptor::class.memberProperties.find { it.name == "typeParameterDescriptors" }!!
            @Suppress("UNCHECKED_CAST") val typedProperty = property as KProperty1<SerialDescriptor, Array<SerialDescriptor>>
            typedProperty.isAccessible = true
            return typedProperty.get(serialDescriptor).toList()
        }
        return emptyList()
    }


    /**
     * @return a [TypeName] for this class
     */
    private fun KClass<*>.toTypeName() = TypeName(
        full = this.qualifiedName ?: this.java.name,
        short = this.simpleName ?: this.java.name
    )

}
