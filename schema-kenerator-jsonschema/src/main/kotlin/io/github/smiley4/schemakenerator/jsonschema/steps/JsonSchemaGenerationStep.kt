package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.CollectionTypeData
import io.github.smiley4.schemakenerator.core.data.EnumTypeData
import io.github.smiley4.schemakenerator.core.data.MapTypeData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.WildcardTypeData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode

/**
 * Generates json-schemas from the given type data. All types in the schema are provisionally referenced by the full type-id.
 * Result needs to be "compiled" to get the final json-schema.
 */
class JsonSchemaGenerationStep {

    private val schemaUtils = JsonSchemaUtils()

    fun generate(bundle: Bundle<BaseTypeData>): Bundle<JsonSchema> {
        val types = listOf(bundle.data) + bundle.supporting
        return Bundle(
            data = generate(bundle.data, types),
            supporting = bundle.supporting.map { generate(it, types) }
        )
    }

    private fun generate(typeData: BaseTypeData, typeDataList: Collection<BaseTypeData>): JsonSchema {
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
            else -> JsonSchema(
                json = schemaUtils.nullSchema(),
                typeData = WildcardTypeData()
            )
        }
    }

    private fun buildAnySchema(): JsonSchema {
        return JsonSchema(schemaUtils.anyObjectSchema(), WildcardTypeData())
    }

    private fun buildPrimitiveSchema(typeData: PrimitiveTypeData): JsonSchema {
        return when (typeData.qualifiedName) {
            Number::class.qualifiedName -> schemaUtils.numericSchema(
                integer = false,
                min = null,
                max = null,
            )
            Byte::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = Byte.MIN_VALUE,
                max = Byte.MAX_VALUE,
            )
            Short::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = Short.MIN_VALUE,
                max = Short.MAX_VALUE,
            )
            Int::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = Int.MIN_VALUE,
                max = Int.MAX_VALUE,
            )
            Long::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = Long.MIN_VALUE,
                max = Long.MAX_VALUE,
            )
            UByte::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = UByte.MIN_VALUE.toLong(),
                max = UByte.MAX_VALUE.toLong(),
            )
            UShort::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = UShort.MIN_VALUE.toLong(),
                max = UShort.MAX_VALUE.toLong(),
            )
            UInt::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = UInt.MIN_VALUE.toLong(),
                max = UInt.MAX_VALUE.toLong(),
            )
            ULong::class.qualifiedName -> schemaUtils.numericSchema(
                integer = true,
                min = null,
                max = null,
            )
            Float::class.qualifiedName -> schemaUtils.numericSchema(
                integer = false,
                min = Float.MIN_VALUE,
                max = Float.MAX_VALUE,
            )
            Double::class.qualifiedName -> schemaUtils.numericSchema(
                integer = false,
                min = Double.MIN_VALUE,
                max = Double.MAX_VALUE,
            )
            Boolean::class.qualifiedName -> schemaUtils.booleanSchema()
            Char::class.qualifiedName -> schemaUtils.stringSchema(
                min = 1,
                max = 1,
            )
            String::class.qualifiedName -> schemaUtils.stringSchema(
                min = null,
                max = null,
            )
            Any::class.qualifiedName -> schemaUtils.anyObjectSchema()
            Unit::class.qualifiedName -> schemaUtils.nullSchema()
            else -> schemaUtils.nullSchema()
        }.let {
            JsonSchema(
                json = it,
                typeData = typeData
            )
        }
    }

    private fun buildEnumSchema(typeData: EnumTypeData): JsonSchema {
        return JsonSchema(
            json = schemaUtils.enumSchema(typeData.enumConstants),
            typeData = typeData
        )
    }

    private fun buildCollectionSchema(typeData: CollectionTypeData): JsonSchema {
        return JsonSchema(
            json = schemaUtils.arraySchema(schemaUtils.referenceSchema(typeData.itemType.type)),
            typeData = typeData
        )
    }

    private fun buildMapSchema(typeData: MapTypeData): JsonSchema {
        return JsonSchema(
            json = schemaUtils.mapObjectSchema(schemaUtils.referenceSchema(typeData.valueType.type)),
            typeData = typeData
        )
    }

    private fun buildWithSubtypes(typeData: ObjectTypeData): JsonSchema {
        return JsonSchema(
            json = schemaUtils.subtypesSchema(
                typeData.subtypes.map { schemaUtils.referenceSchema(it.full()) }
            ),
            typeData = typeData
        )
    }

    private fun buildObjectSchema(typeData: ObjectTypeData, typeDataList: Collection<BaseTypeData>): JsonSchema {

        val requiredProperties = mutableSetOf<String>()
        val propertySchemas = mutableMapOf<String, JsonNode>()

        collectMembers(typeData, typeDataList).forEach { member ->
            propertySchemas[member.name] = schemaUtils.referenceSchema(member.type)
            if (!member.nullable) {
                requiredProperties.add(member.name)
            }
        }

        return JsonSchema(
            json = schemaUtils.objectSchema(propertySchemas, requiredProperties),
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
