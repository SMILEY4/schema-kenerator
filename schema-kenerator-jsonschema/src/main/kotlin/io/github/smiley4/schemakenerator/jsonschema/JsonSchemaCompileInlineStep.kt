package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaCompileUtils.resolveReferences
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

internal class JsonSchemaCompileInlineStep(private val explicitNullTypes: Boolean) {

    private val schemaUtils = JsonSchemaUtils()


    /**
     * Inline all referenced schema
     */
    fun compile(input: IntermediateJsonSchemaData): CompiledJsonSchemaData {
        val root = resolveReferences(input.rootSchema) { refObj ->
            resolveReference(input, refObj)
        }
        return CompiledJsonSchemaData(
            typeData = input.rootTypeData,
            json = root,
            definitions = emptyMap()
        )
    }

    private fun resolveReference(input: IntermediateJsonSchemaData, refObj: JsonObject): JsonNode {
        // find actual schema data
        val referencedSchema = input[TypeId((refObj.getText("\$ref")))]
        // no schema data found -> fallback to original property
        if (referencedSchema == null) {
            return refObj
        }
        // create replacement property
        return if (referencedSchema.typeData.id == input.rootId) {
            schemaUtils.referenceSelf()
        } else {
            createInlining(refObj, referencedSchema)
        }
    }


    /**
     * Create an inline json-schema to replace the pending referencing schema.
     * @param refObj the schema containing the reference
     * @param schema the schema referenced by [refObj]
     */
    private fun createInlining(refObj: JsonObject, schema: JsonSchemaData): JsonNode {
        return schema.json.copyNode().also {
            if (it is JsonObject) {
                it.properties.putAll(buildMap {
                    this.putAll(refObj.properties)
                    this.remove("\$ref")
                })
                if (it.properties.contains("_nullable") && it.getBool("_nullable") && explicitNullTypes) {
                    setNullable(it)
                    it.properties.remove("_nullable")
                }
            }
        }
    }


    /**
     * Explicitly mark the given schema as a nullable type
     */
    private fun setNullable(schema: JsonObject) {
        if (schema.properties.contains("type") && schema.properties["type"] is JsonTextValue) {
            schema.properties["type"] = JsonArray(
                mutableListOf(
                    JsonTextValue(schema.getText("type")),
                )
            )
        }
        if (schema.properties.contains("type") && schema.properties["type"] is JsonArray) {
            schema.getArray("type").items.add(JsonTextValue("null"))
        }
        if (schema.properties.contains("anyOf") && schema.properties["anyOf"] is JsonArray && schema.getArray("anyOf").items.isNotEmpty()) {
            schema.getArray("anyOf").items.add(schemaUtils.nullSchema())
        }
        if (schema.properties.contains("oneOf") && schema.properties["oneOf"] is JsonArray && schema.getArray("oneOf").items.isNotEmpty()) {
            schema.getArray("oneOf").items.add(schemaUtils.nullSchema())
        }
    }

}
