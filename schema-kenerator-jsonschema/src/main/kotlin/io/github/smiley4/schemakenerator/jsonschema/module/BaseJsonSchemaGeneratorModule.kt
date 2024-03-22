package io.github.smiley4.schemakenerator.jsonschema.module

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerator
import io.github.smiley4.schemakenerator.jsonschema.json.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.json.JsonNullValue
import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchema

class BaseJsonSchemaGeneratorModule : JsonSchemaGeneratorModule {

    private val schema = JsonSchema()

    override fun build(generator: JsonSchemaGenerator, context: TypeParserContext, typeData: BaseTypeData): JsonObject {
        if (typeData is ObjectTypeData && typeData.subtypes.isNotEmpty()) {
            return buildWithSubtypes(generator, typeData, context)
        }
        return when (typeData) {
            is PrimitiveTypeData -> buildPrimitiveSchema(typeData)
            is EnumTypeData -> buildEnumSchema(typeData)
            is CollectionTypeData -> buildCollectionSchema(generator, typeData, context)
            is MapTypeData -> buildMapSchema(generator, typeData, context)
            is ObjectTypeData -> buildObjectSchema(generator, typeData, context)
            is WildcardTypeData -> buildAnySchema()
            else -> schema.nullSchema()
        }
    }


    override fun enhance(generator: JsonSchemaGenerator, context: TypeParserContext, typeData: BaseTypeData, node: JsonObject) {
        // nothing to do
    }


    private fun buildWithSubtypes(generator: JsonSchemaGenerator, typeData: ObjectTypeData, context: TypeParserContext): JsonObject {
        return schema.subtypesSchema(typeData.subtypes.map { subtype -> generator.generate(subtype, context) })
    }


    private fun buildEnumSchema(typeData: EnumTypeData): JsonObject {
        return schema.enumSchema(typeData.enumConstants)
    }


    private fun buildObjectSchema(generator: JsonSchemaGenerator, typeData: ObjectTypeData, context: TypeParserContext): JsonObject {
        val requiredProperties = mutableSetOf<String>()
        val propertySchemas = mutableMapOf<String, JsonNode>()

        typeData.members.forEach { member ->
            val memberSchema = generator.generate(member.type, context)
            propertySchemas[member.name] = memberSchema
            if (!member.nullable) {
                requiredProperties.add(member.name)
            }
        }

        return schema.objectSchema(propertySchemas, requiredProperties)
    }


    private fun buildCollectionSchema(generator: JsonSchemaGenerator, typeData: CollectionTypeData, context: TypeParserContext): JsonObject {
        val itemSchema = generator.generate(typeData.itemType.type, context)
        return schema.arraySchema(
            items = itemSchema,
        )
    }


    private fun buildMapSchema(generator: JsonSchemaGenerator, typeData: MapTypeData, context: TypeParserContext): JsonObject {
        val valueSchema = generator.generate(typeData.valueType.type, context)
        return schema.mapObjectSchema(valueSchema)
    }


    private fun buildAnySchema(): JsonObject {
        return schema.anyObjectSchema()
    }

    private fun buildPrimitiveSchema(typeData: PrimitiveTypeData): JsonObject {
        return when (typeData.qualifiedName) {
            Number::class.qualifiedName -> schema.numericSchema(
                integer = false,
                min = null,
                max = null,
            )
            Byte::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = Byte.MIN_VALUE,
                max = Byte.MAX_VALUE,
            )
            Short::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = Short.MIN_VALUE,
                max = Short.MAX_VALUE,
            )
            Int::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = Int.MIN_VALUE,
                max = Int.MAX_VALUE,
            )
            Long::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = Long.MIN_VALUE,
                max = Long.MAX_VALUE,
            )
            UByte::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = UByte.MIN_VALUE.toLong(),
                max = UByte.MAX_VALUE.toLong(),
            )
            UShort::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = UShort.MIN_VALUE.toLong(),
                max = UShort.MAX_VALUE.toLong(),
            )
            UInt::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = UInt.MIN_VALUE.toLong(),
                max = UInt.MAX_VALUE.toLong(),
            )
            ULong::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = null,
                max = null,
            )
            Float::class.qualifiedName -> schema.numericSchema(
                integer = false,
                min = Float.MIN_VALUE,
                max = Float.MAX_VALUE,
            )
            Double::class.qualifiedName -> schema.numericSchema(
                integer = false,
                min = Double.MIN_VALUE,
                max = Double.MAX_VALUE,
            )
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
        }
    }

}