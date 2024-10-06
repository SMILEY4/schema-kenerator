package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileUtils.resolveReferences

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
            val referencedId = TypeId.parse((refObj.properties["\$ref"] as JsonTextValue).value)
            val referencedSchema = schemaList.find(referencedId)
            if (referencedSchema != null) {
                // todo: bug: referencedSchema.json needs to be copied here!! Otherwise: collisions with other usages
                referencedSchema.json.also {
                    if (it is JsonObject) {
                        it.properties.putAll(buildMap {
                            this.putAll(refObj.properties)
                            this.remove("\$ref")
                        })
                    }
                }
            } else {
                refObj
            }
        }
        return CompiledJsonSchema(
            json = root,
            typeData = bundle.data.typeData,
            definitions = emptyMap()
        )
    }

    private fun Collection<JsonSchema>.find(id: TypeId): JsonSchema? {
        return this.find { it.typeData.id == id }
    }

}
