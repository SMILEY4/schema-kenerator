package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import io.github.smiley4.schemakenerator.jsonschema.json.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchemaUtils

class JsonSchemaGenerator {

    private val schema = JsonSchemaUtils()

    fun generate(types: Collection<BaseTypeData>): List<JsonSchema> {
        return types.map { generate(it, types) }
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
                json = schema.nullSchema(),
                typeId = TypeId.unknown()
            )
        }
    }

    private fun buildAnySchema(): JsonSchema {
        return JsonSchema(schema.anyObjectSchema(), TypeId.wildcard())
    }

    private fun buildPrimitiveSchema(typeData: PrimitiveTypeData): JsonSchema {
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
        }.let {
            JsonSchema(
                json = it,
                typeId = typeData.id
            )
        }
    }

    private fun buildEnumSchema(typeData: EnumTypeData): JsonSchema {
        return JsonSchema(
            json = schema.enumSchema(typeData.enumConstants),
            typeId = typeData.id
        )
    }

    private fun buildCollectionSchema(typeData: CollectionTypeData): JsonSchema {
        return JsonSchema(
            json = schema.arraySchema(schema.referenceSchema(typeData.itemType.type)),
            typeId = typeData.id
        )
    }

    private fun buildMapSchema(typeData: MapTypeData): JsonSchema {
        return JsonSchema(
            json = schema.mapObjectSchema(schema.referenceSchema(typeData.valueType.type)),
            typeId = typeData.id
        )
    }

    private fun buildWithSubtypes(typeData: ObjectTypeData): JsonSchema {
        return JsonSchema(
            json = schema.subtypesSchema(
                typeData.subtypes.map { schema.referenceSchema(it.id) }
            ),
            typeId = typeData.id
        )
    }
    
    private fun buildObjectSchema(typeData: ObjectTypeData, typeDataList: Collection<BaseTypeData>): JsonSchema {
        
        val requiredProperties = mutableSetOf<String>()
        val propertySchemas = mutableMapOf<String, JsonNode>()

        collectMembers(typeData, typeDataList).forEach { member ->
            propertySchemas[member.name] = schema.referenceSchema(member.type)
            if (!member.nullable) {
                requiredProperties.add(member.name)
            }
        }

        return JsonSchema(
            json = schema.objectSchema(propertySchemas, requiredProperties),
            typeId = typeData.id
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
