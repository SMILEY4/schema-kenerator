package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.WildcardTypeData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonValue
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.array
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.obj
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType

/**
 * Resolves references in schemas - either inlining them or collecting them in the definitions-section and referencing them.
 * - input: [JsonSchema]
 * - output: [CompiledJsonSchema]
 */
class JsonSchemaCompileStep(private val pathType: TitleType = TitleType.FULL) {

    private val schemaUtils = JsonSchemaUtils()


    /**
     * Inline all referenced schema
     */
    fun compileInlining(schemas: Collection<JsonSchema>): List<CompiledJsonSchema> {
        return schemas.map { schema ->
            CompiledJsonSchema(
                json = inlineReferences(schema.json, schemas),
                typeData = schema.typeData,
                definitions = emptyMap()
            )
        }
    }


    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compileReferencing(schemas: Collection<JsonSchema>): List<CompiledJsonSchema> {
        return schemas.map { schema ->
            val result = referenceDefinitionsReferences(schema.json, schemas)
            CompiledJsonSchema(
                typeData = schema.typeData,
                json = result.json,
                definitions = result.definitions
            )
        }
    }

    /**
     * Put referenced schemas and root-schema into definitions and reference them
     */
    fun compileReferencingRoot(schemas: Collection<JsonSchema>): List<CompiledJsonSchema> {
        return compileReferencing(schemas).map {
            if (shouldReference(it.json)) {
                CompiledJsonSchema(
                    typeData = it.typeData,
                    json = schemaUtils.referenceSchema(getRefPath(it.typeData.id), true),
                    definitions = buildMap {
                        this.putAll(it.definitions)
                        this[it.typeData.id] = it.json
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
            val referencedSchema = schemaList.find { it.typeData.id == referencedId }!!
            inlineReferences(referencedSchema.json, schemaList).also {
                if(it is JsonObject) {
                    it.properties.putAll(buildMap {
                        this.putAll(refObj.properties)
                        this.remove("\$ref")
                    })
                }
            }
        }
    }

    private fun referenceDefinitionsReferences(node: JsonNode, schemaList: Collection<JsonSchema>): CompiledJsonSchema {
        val definitions = mutableMapOf<TypeId, JsonNode>()
        val json = replaceReferences(node) { refObj ->
            val referencedId = TypeId.parse((refObj.properties["\$ref"] as JsonTextValue).value)
            val referencedSchema = schemaList.find { it.typeData.id == referencedId }!!
            val procReferencedSchema = referenceDefinitionsReferences(referencedSchema.json, schemaList).also {
                if(it.json is JsonObject) {
                    it.json.properties.putAll(buildMap {
                        this.putAll(refObj.properties)
                        this.remove("\$ref")
                    })
                }
            }
            if (shouldReference(referencedSchema.json)) {
                definitions[referencedId] = procReferencedSchema.json
                definitions.putAll(procReferencedSchema.definitions)
                schemaUtils.referenceSchema(getRefPath(referencedId), true)
            } else {
                definitions.putAll(procReferencedSchema.definitions)
                procReferencedSchema.json
            }
        }
        return CompiledJsonSchema(
            typeData = WildcardTypeData(),
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
        if (node is JsonObject && node.properties.containsKey("\$ref")) {
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
