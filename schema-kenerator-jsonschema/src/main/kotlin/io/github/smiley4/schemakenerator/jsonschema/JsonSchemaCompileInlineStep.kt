package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaCompileUtils.resolveReferences
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

internal class JsonSchemaCompileInlineStep {

    /**
     * Inline all referenced schema
     */
    fun compile(input: IntermediateJsonSchemaData): CompiledJsonSchemaData {
        val root = resolveReferences(input.rootSchema) { refObj ->
            val referencedSchema = input[TypeId((refObj.properties["\$ref"] as JsonTextValue).value)]
            if (referencedSchema == null) {
                refObj
            } else {
                createInlining(refObj, referencedSchema)
            }
        }
        return CompiledJsonSchemaData(
            typeData = input.rootTypeData,
            json = root,
            definitions = emptyMap()
        )
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
            }
        }
    }

}
