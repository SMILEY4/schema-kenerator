package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.json.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.json.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.json.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.json.JsonValue
import io.github.smiley4.schemakenerator.jsonschema.json.array
import io.github.smiley4.schemakenerator.jsonschema.json.obj
import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchemaUtils
import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchemaWithDefinitions

/**
 * Resolves references in schemas - either inlining them or collecting them in the definitions-section and referncing them.
 */
class JsonSchemaCompiler(val pathType: TitleType = TitleType.FULL) {

    private val schema = JsonSchemaUtils()

    fun compileInlining(schemas: Collection<JsonSchema>): List<JsonSchema> {
        return schemas.map { schema ->
            JsonSchema(
                json = inlineReferences(schema.json, schemas),
                typeId = schema.typeId
            )
        }
    }

    fun compileReferencing(schemas: Collection<JsonSchema>): List<JsonSchemaWithDefinitions> {
        return schemas.map { schema ->
            val result = referenceDefinitionsReferences(schema.json, schemas)
            JsonSchemaWithDefinitions(
                typeId = schema.typeId,
                json = result.json,
                definitions = result.definitions
            )
        }
    }

    fun compileReferencingRoot(schemas: Collection<JsonSchema>): List<JsonSchemaWithDefinitions> {
        return compileReferencing(schemas).map {
            if (shouldReference(it.json)) {
                JsonSchemaWithDefinitions(
                    typeId = it.typeId,
                    json = schema.referenceSchema(getRefPath(it.typeId), true),
                    definitions = buildMap {
                        this.putAll(it.definitions)
                        this[it.typeId] = it.json
                    }
                )
            } else {
                it
            }
        }
    }

    private fun inlineReferences(node: JsonNode, schemaList: Collection<JsonSchema>): JsonNode {
        return replaceReferences(node) { refObj ->
            val referencedId = TypeId.parse((refObj.properties["\$ref"] as JsonTextValue).value)
            val referencedSchema = schemaList.find { it.typeId == referencedId }!!
            inlineReferences(referencedSchema.json, schemaList)
        }
    }

    private fun referenceDefinitionsReferences(node: JsonNode, schemaList: Collection<JsonSchema>): JsonSchemaWithDefinitions {
        val definitions = mutableMapOf<TypeId, JsonNode>()
        val json = replaceReferences(node) { refObj ->
            val referencedId = TypeId.parse((refObj.properties["\$ref"] as JsonTextValue).value)
            val referencedSchema = schemaList.find { it.typeId == referencedId }!!
            val procReferencedSchema = referenceDefinitionsReferences(referencedSchema.json, schemaList)
            if (shouldReference(referencedSchema.json)) {
                definitions[referencedId] = procReferencedSchema.json
                definitions.putAll(procReferencedSchema.definitions)
                schema.referenceSchema(getRefPath(referencedId), true)
            } else {
                definitions.putAll(procReferencedSchema.definitions)
                procReferencedSchema.json
            }
        }
        return JsonSchemaWithDefinitions(
            typeId = TypeId.unknown(),
            json = json,
            definitions = definitions
        )
    }

    private fun shouldReference(json: JsonNode): Boolean {
        val complexProperties = setOf(
            "required",
            "properties"
        )
        return if (json is JsonObject) {
            (getTextProperty(json, "type") == "object" && json.properties.keys.any { complexProperties.contains(it) } && !existsProperty(json, "additionalProperties"))
                    || existsProperty(json, "enum")
                    || existsProperty(json, "anyOf")
        } else {
            false
        }

    }

    private fun getTextProperty(node: JsonNode, key: String): String? {
        if (node is JsonObject) {
            val type = node.properties[key]
            if (type is JsonTextValue) {
                return type.value
            }
        }
        return null
    }

    private fun existsProperty(node: JsonNode, key: String): Boolean {
        if (node is JsonObject) {
            return node.properties[key] != null
        }
        return false
    }

    private fun replaceReferences(node: JsonNode, mapping: (refObj: JsonObject) -> JsonNode): JsonNode {
        if (node is JsonObject && node.properties.size == 1 && node.properties["\$ref"] != null) {
            return mapping(node)
        } else {
            return when (node) {
                is JsonArray -> array {
                    node.items
                        .map { replaceReferences(it, mapping) }
                        .forEach { item(it) }
                }
                is JsonObject -> {
                    obj {
                        node.properties.forEach { (key, value) ->
                            key to replaceReferences(value, mapping)
                        }
                    }
                }
                is JsonValue<*> -> node
            }
        }
    }

    private fun getRefPath(typeId: TypeId): String {
        return when(pathType) {
            TitleType.FULL -> typeId.full()
            TitleType.SIMPLE -> typeId.simple()
        }
    }

}
