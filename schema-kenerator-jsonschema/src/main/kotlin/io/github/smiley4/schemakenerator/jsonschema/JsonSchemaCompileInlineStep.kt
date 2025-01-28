package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaCompileUtils.resolveReferences

/**
 * Resolves references in prepared json-schemas by inlining them.
 */
class JsonSchemaCompileInlineStep {

    /**
     * Inline all referenced schema
     */
    fun compile(bundle: Bundle<JsonSchema>): CompiledJsonSchema {
        val schemaList = bundle.flatten()
        val root = resolveReferences(bundle.data.json) { refObj ->
            val referencedSchema = schemaList.find(TypeId((refObj.properties["\$ref"] as JsonTextValue).value))
            if (referencedSchema == null) {
                refObj
            } else {
                createInlining(refObj, referencedSchema)
            }
        }
        return CompiledJsonSchema(
            json = root,
            typeData = bundle.data.typeData,
            definitions = emptyMap()
        )
    }


    /**
     * Create an inline json-schema to replace the pending referencing schema.
     * @param refObj the schema containing the reference
     * @param schema the schema referenced by [refObj]
     */
    private fun createInlining(refObj: JsonObject, schema: JsonSchema): JsonNode {
        return schema.json.copyNode().also {
            if (it is JsonObject) {
                it.properties.putAll(buildMap {
                    this.putAll(refObj.properties)
                    this.remove("\$ref")
                })
            }
        }
    }


    /**
     * @return the [JsonSchema] for the given [TypeId]
     */
    private fun Collection<JsonSchema>.find(id: TypeId): JsonSchema? = this.find { it.typeData.id == id }

}
