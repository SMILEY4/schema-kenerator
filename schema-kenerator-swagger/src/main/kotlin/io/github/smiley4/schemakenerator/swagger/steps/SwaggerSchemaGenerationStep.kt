package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.CollectionTypeData
import io.github.smiley4.schemakenerator.core.data.EnumTypeData
import io.github.smiley4.schemakenerator.core.data.MapTypeData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.PropertyType
import io.github.smiley4.schemakenerator.core.data.WildcardTypeData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.swagger.v3.oas.models.media.Schema
import java.math.BigDecimal

/**
 * Generates swagger-schemas from the given type data. All types in the schema are provisionally referenced by the full type-id.
 * Result needs to be "compiled" to get the final swagger-schema.
 */
class SwaggerSchemaGenerationStep(private val optionalAsNonRequired: Boolean = false) {

    private val schema = SwaggerSchemaUtils()

    fun generate(bundle: Bundle<BaseTypeData>): Bundle<SwaggerSchema> {
        val allTypeData = listOf(bundle.data) + bundle.supporting
        return Bundle(
            data = generate(bundle.data, allTypeData),
            supporting = bundle.supporting.map { generate(it, allTypeData) }
        )
    }

    private fun generate(typeData: BaseTypeData, typeDataList: Collection<BaseTypeData>): SwaggerSchema {
        if (typeData is ObjectTypeData && typeData.subtypes.isNotEmpty()) {
            return buildWithSubtypes(typeData)
        }
        return when (typeData) {
            is PrimitiveTypeData -> buildPrimitiveSchema(typeData)
            is EnumTypeData -> buildEnumSchema(typeData)
            is CollectionTypeData -> buildCollectionSchema(typeData)
            is MapTypeData -> buildMapSchema(typeData)
            is ObjectTypeData -> buildObjectSchema(typeData, typeDataList)
            is WildcardTypeData -> buildAnySchema()
            else -> SwaggerSchema(
                swagger = schema.nullSchema(),
                typeData = WildcardTypeData()
            )
        }
    }

    private fun buildAnySchema(): SwaggerSchema {
        return SwaggerSchema(schema.anyObjectSchema(), WildcardTypeData())
    }

    private fun buildPrimitiveSchema(typeData: PrimitiveTypeData): SwaggerSchema {
        return when (typeData.qualifiedName) {
            Number::class.qualifiedName -> schema.numberSchema(false)
            Byte::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(Byte.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(Byte.MAX_VALUE.toLong()),
            )
            Short::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(Short.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(Short.MAX_VALUE.toLong()),
            )
            Int::class.qualifiedName -> schema.int32Schema()
            Long::class.qualifiedName -> schema.int64Schema()
            UByte::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(UByte.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(UByte.MAX_VALUE.toLong()),
            )
            UShort::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(UShort.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(UShort.MAX_VALUE.toLong()),
            )
            UInt::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(UInt.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(UInt.MAX_VALUE.toLong()),
            )
            ULong::class.qualifiedName -> schema.numberSchema(true)
            Float::class.qualifiedName -> schema.floatSchema()
            Double::class.qualifiedName -> schema.doubleSchema()
            Boolean::class.qualifiedName -> schema.booleanSchema()
            Char::class.qualifiedName -> schema.stringSchema(
                min = 1,
                max = 1,
            )
            String::class.qualifiedName -> schema.stringSchema(
                min = null,
                max = null,
            )
            Any::class.qualifiedName -> schema.anyObjectSchema()
            Unit::class.qualifiedName -> schema.nullSchema()
            else -> schema.nullSchema()
        }.let {
            SwaggerSchema(
                swagger = it,
                typeData = typeData
            )
        }
    }

    private fun buildEnumSchema(typeData: EnumTypeData): SwaggerSchema {
        return SwaggerSchema(
            swagger = schema.enumSchema(typeData.enumConstants),
            typeData = typeData
        )
    }

    private fun buildCollectionSchema(typeData: CollectionTypeData): SwaggerSchema {
        return SwaggerSchema(
            swagger = schema.arraySchema(
                schema.referenceSchema(typeData.itemType.type),
                typeData.unique
            ),
            typeData = typeData
        )
    }

    private fun buildMapSchema(typeData: MapTypeData): SwaggerSchema {
        return SwaggerSchema(
            swagger = schema.mapObjectSchema(schema.referenceSchema(typeData.valueType.type)),
            typeData = typeData
        )
    }

    private fun buildWithSubtypes(typeData: ObjectTypeData): SwaggerSchema {
        return SwaggerSchema(
            swagger = schema.subtypesSchema(
                typeData.subtypes.map { schema.referenceSchema(it.full()) }
            ),
            typeData = typeData
        )
    }

    private fun buildObjectSchema(typeData: ObjectTypeData, typeDataList: Collection<BaseTypeData>): SwaggerSchema {
        if (typeData.isInlineValue) {
            return buildInlineObjectSchema(typeData, typeDataList)
        }

        val requiredProperties = mutableSetOf<String>()
        val propertySchemas = mutableMapOf<String, Schema<*>>()

        collectMembers(typeData, typeDataList).forEach { member ->
            propertySchemas[member.name] = schema.referenceSchema(member.type)
            val nullable = member.nullable
            val optional = member.optional && optionalAsNonRequired
            if(!nullable && !optional) {
                requiredProperties.add(member.name)
            }
        }

        return SwaggerSchema(
            swagger = schema.objectSchema(propertySchemas, requiredProperties),
            typeData = typeData
        )
    }

    private fun buildInlineObjectSchema(typeData: ObjectTypeData, typeDataList: Collection<BaseTypeData>): SwaggerSchema {
        val inlineType = typeData.members.first { it.kind == PropertyType.PROPERTY }
        val inlineTypeData = typeDataList.find { it.id == inlineType.type }
            ?: throw IllegalStateException("Could not find type-data for inline type ${inlineType.type}")
        val inlineTypeSchema = generate(inlineTypeData, typeDataList)
        return SwaggerSchema(
            swagger = inlineTypeSchema.swagger,
            typeData = typeData
        )
    }

    private fun collectMembers(typeData: ObjectTypeData, typeDataList: Collection<BaseTypeData>): List<PropertyData> {
        return buildList {
            addAll(typeData.members)
            typeData.supertypes.forEach { supertypeId ->
                val supertype = typeDataList.find { it.id == supertypeId }
                if (supertype is ObjectTypeData) {
                    addAll(collectMembers(supertype, typeDataList))
                }
            }
        }
    }

}
