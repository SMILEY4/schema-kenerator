package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.obj

internal class JsonSchemaMergeStep(
    private val definitionsPath: String
) {

    /**
     * Merge referenced schemas into the definitions section of the root schema.
     */
    fun merge(input: CompiledJsonSchemaData): CompiledJsonSchemaData {
        val root = input.json
        if (root !is JsonObject || input.definitions.isEmpty()) {
            return input
        }

        root.properties[definitionsPath] = obj {
            input.definitions.forEach { (defKey, def) ->
                defKey to def
            }
        }

        return CompiledJsonSchemaData(
            typeData = input.typeData,
            json = root,
            definitions = emptyMap()
        )
    }

}
