package io.github.smiley4.schemakenerator.jsonschema.module

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerator
import io.github.smiley4.schemakenerator.jsonschema.json.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.json.obj
import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchema

class ReferencingJsonSchemaGeneratorModule(private val definitionKey: String = "definitions") : JsonSchemaGeneratorModule {

    private val jsonSchema = JsonSchema()


    override fun build(generator: JsonSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData): JsonObject {
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
            else -> jsonSchema.nullSchema()
        }
    }

    override fun enhance(generator: JsonSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, node: JsonObject) = Unit


    private fun buildWithSubtypes(generator: JsonSchemaGenerator, typeData: ObjectTypeData, context: TypeDataContext): JsonObject {
        return jsonSchema.subtypesSchema(typeData.subtypes.map { subtype -> generator.generate(subtype, context) })
    }


    private fun buildEnumSchema(typeData: EnumTypeData): JsonObject {
        return jsonSchema.enumSchema(typeData.enumConstants)
    }


    private fun buildObjectSchema(generator: JsonSchemaGenerator, typeData: ObjectTypeData, context: TypeDataContext): JsonObject {
        val requiredProperties = mutableSetOf<String>()
        val propertySchemas = mutableMapOf<String, JsonNode>()

        typeData.members.forEach { member ->
            if (shouldReference(member.type.resolve(context))) {
                propertySchemas[member.name] = jsonSchema.referenceSchema(member.type.idStr())
            } else {
                propertySchemas[member.name] = generator.generate(member.type, context)
            }
            if (!member.nullable) {
                requiredProperties.add(member.name)
            }
        }

        return jsonSchema.objectSchema(propertySchemas, requiredProperties)
            .also {
                val definitions = obj {
                    typeData.members.forEach { member ->
                        if (shouldReference(member.type.resolve(context))) {
                            member.type.idStr() to generator.generate(member.type, context)
                        }
                    }
                }
                if (definitions.properties.isNotEmpty()) {
                    it.properties[definitionKey] = definitions
                }
            }.also {
                pullUpDefinitions(it)
            }
    }

    private fun buildCollectionSchema(generator: JsonSchemaGenerator, typeData: CollectionTypeData, context: TypeDataContext): JsonObject {
        if (shouldReference(typeData.itemType.type.resolve(context))) {
            return jsonSchema.arraySchema(
                items = jsonSchema.referenceSchema(typeData.itemType.type.idStr())
            ).also {
                val definitions = obj {
                    typeData.itemType.type.idStr() to generator.generate(typeData.itemType.type, context)
                }
                if (definitions.properties.isNotEmpty()) {
                    it.properties[definitionKey] = definitions
                }
            }.also {
                pullUpDefinitions(it)
            }
        } else {
            return jsonSchema.arraySchema(
                items = generator.generate(typeData.itemType.type, context),
            )
        }
    }


    private fun buildMapSchema(generator: JsonSchemaGenerator, typeData: MapTypeData, context: TypeDataContext): JsonObject {
        val valueSchema = generator.generate(typeData.valueType.type, context)
        return jsonSchema.mapObjectSchema(valueSchema)
    }


    private fun buildAnySchema(): JsonObject {
        return jsonSchema.anyObjectSchema()
    }

    private fun buildPrimitiveSchema(typeData: PrimitiveTypeData): JsonObject {
        return when (typeData.qualifiedName) {
            Number::class.qualifiedName -> jsonSchema.numericSchema(
                integer = false,
                min = null,
                max = null,
            )
            Byte::class.qualifiedName -> jsonSchema.numericSchema(
                integer = true,
                min = Byte.MIN_VALUE,
                max = Byte.MAX_VALUE,
            )
            Short::class.qualifiedName -> jsonSchema.numericSchema(
                integer = true,
                min = Short.MIN_VALUE,
                max = Short.MAX_VALUE,
            )
            Int::class.qualifiedName -> jsonSchema.numericSchema(
                integer = true,
                min = Int.MIN_VALUE,
                max = Int.MAX_VALUE,
            )
            Long::class.qualifiedName -> jsonSchema.numericSchema(
                integer = true,
                min = Long.MIN_VALUE,
                max = Long.MAX_VALUE,
            )
            UByte::class.qualifiedName -> jsonSchema.numericSchema(
                integer = true,
                min = UByte.MIN_VALUE.toLong(),
                max = UByte.MAX_VALUE.toLong(),
            )
            UShort::class.qualifiedName -> jsonSchema.numericSchema(
                integer = true,
                min = UShort.MIN_VALUE.toLong(),
                max = UShort.MAX_VALUE.toLong(),
            )
            UInt::class.qualifiedName -> jsonSchema.numericSchema(
                integer = true,
                min = UInt.MIN_VALUE.toLong(),
                max = UInt.MAX_VALUE.toLong(),
            )
            ULong::class.qualifiedName -> jsonSchema.numericSchema(
                integer = true,
                min = null,
                max = null,
            )
            Float::class.qualifiedName -> jsonSchema.numericSchema(
                integer = false,
                min = Float.MIN_VALUE,
                max = Float.MAX_VALUE,
            )
            Double::class.qualifiedName -> jsonSchema.numericSchema(
                integer = false,
                min = Double.MIN_VALUE,
                max = Double.MAX_VALUE,
            )
            Boolean::class.qualifiedName -> jsonSchema.booleanSchema()
            Char::class.qualifiedName -> jsonSchema.stringSchema(
                min = 1,
                max = 1,
            )
            String::class.qualifiedName -> jsonSchema.stringSchema(
                min = null,
                max = null,
            )
            Any::class.qualifiedName -> jsonSchema.anyObjectSchema()
            Unit::class.qualifiedName -> jsonSchema.nullSchema()
            else -> jsonSchema.nullSchema()
        }
    }

    private fun pullUpDefinitions(schema: JsonObject) {

        println(schema.prettyPrint())

        val rootDefinitions = mutableMapOf<String, JsonNode>()

        // collect nested definitions in properties of given schema
        schema.properties["properties"]?.also { properties ->
            (properties as JsonObject).properties.forEach { (_, propertySchema) ->
                if (propertySchema is JsonObject && propertySchema.properties.containsKey(definitionKey)) {
                    val definitions = propertySchema.properties[definitionKey]
                    (definitions as JsonObject).properties.forEach { (definitionName, definitionSchema) ->
                        rootDefinitions[definitionName] = definitionSchema
                    }
                    propertySchema.properties.remove(definitionKey)
                }
            }
        }

        // collect existing nested definitions
        schema.properties[definitionKey]?.also { schemaDefinitions ->
            (schemaDefinitions as JsonObject).properties.forEach { (_, definitionSchema) ->
                if (definitionSchema is JsonObject && definitionSchema.properties.containsKey(definitionKey)) {
                    val definitions = definitionSchema.properties[definitionKey]
                    (definitions as JsonObject).properties.forEach { (definitionName, definitionSchema) ->
                        rootDefinitions[definitionName] = definitionSchema
                    }
                    definitionSchema.properties.remove(definitionKey)
                }
            }
        }

        // collect existing definitions of given schema
        schema.properties[definitionKey]?.also { schemaDefinitions ->
            (schemaDefinitions as JsonObject).properties.forEach { (definitionName, definitionSchema) ->
                rootDefinitions[definitionName] = definitionSchema
            }
        }

        // set updated definitions of given schema
        if (rootDefinitions.isEmpty()) {
            schema.properties.remove(definitionKey)
        } else {
            schema.properties[definitionKey] = obj {
                rootDefinitions.forEach { (name, schema) -> name to schema }
            }
        }
    }

    private fun shouldReference(type: BaseTypeData?): Boolean {
        return type is ObjectTypeData && type !is CollectionTypeData && type !is MapTypeData && type !is EnumTypeData
    }
}