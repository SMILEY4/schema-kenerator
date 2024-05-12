package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.WildcardTypeData
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonValue
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.array
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.obj

/**
 * Resolves references in prepared json-schemas - either inlining them or collecting them in the definitions-section and referencing them.
 * @param pathType when referencing types, either use the full id of a shorter, simplified version
 */
class JsonSchemaCompileStep(private val pathType: TitleType = TitleType.FULL) {

    private val schemaUtils = JsonSchemaUtils()


    /**
     * Inline all referenced schema
     */
    fun compileInlining(bundle: Bundle<JsonSchema>): CompiledJsonSchema {
        return CompiledJsonSchema(
            json = inlineReferences(bundle.data.json, bundle.supporting),
            typeData = bundle.data.typeData,
            definitions = emptyMap()
        )
    }

    private fun inlineReferences(node: JsonNode, schemaList: Collection<JsonSchema>): JsonNode {
        return replaceReferences(node) { refObj ->
            val referencedId = TypeId.parse((refObj.properties["\$ref"] as JsonTextValue).value)
            val referencedSchema = schemaList.find { it.typeData.id == referencedId }!!
            inlineReferences(referencedSchema.json, schemaList).also {
                if (it is JsonObject) {
                    it.properties.putAll(buildMap {
                        this.putAll(refObj.properties)
                        this.remove("\$ref")
                    })
                }
            }
        }
    }


    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compileReferencing(bundle: Bundle<JsonSchema>): CompiledJsonSchema {
        val result = referenceDefinitionsReferences(bundle.data.json, bundle.supporting)
        return CompiledJsonSchema(
            typeData = bundle.data.typeData,
            json = result.json,
            definitions = result.definitions
        )
    }


    /**
     * Put referenced schemas and root-schema into definitions and reference them
     */
    fun compileReferencingRoot(bundle: Bundle<JsonSchema>): CompiledJsonSchema {
        val result = compileReferencing(bundle)
        return if (shouldReference(bundle.data.json)) {
            CompiledJsonSchema(
                typeData = result.typeData,
                json = schemaUtils.referenceSchema(getRefPath(result.typeData.id), true),
                definitions = buildMap {
                    this.putAll(result.definitions)
                    this[result.typeData.id] = result.json
                }
            )
        } else {
            result
        }
    }

    private fun referenceDefinitionsReferences(node: JsonNode, schemaList: Collection<JsonSchema>): CompiledJsonSchema {
        val definitions = mutableMapOf<TypeId, JsonNode>()
        val json = replaceReferences(node) { refObj ->
            val referencedId = TypeId.parse((refObj.properties["\$ref"] as JsonTextValue).value)
            val referencedSchema = schemaList.find { it.typeData.id == referencedId }!!
            val procReferencedSchema = referenceDefinitionsReferences(referencedSchema.json, schemaList).also {
                if (it.json is JsonObject) {
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
            (getTextProperty(json, "type") == "object" && json.properties.keys.any { complexProperties.contains(it) } && !existsProperty(
                json,
                "additionalProperties"
            ))
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
        return when (pathType) {
            TitleType.FULL -> typeId.full()
            TitleType.SIMPLE -> typeId.simple()
        }
    }

}
