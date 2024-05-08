package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.JsonTypeHint
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

/**
 * Modifies type from annotation [JsonTypeHint]
 * - input: [JsonSchema]
 * - output: [JsonSchema] with modified type from annotations
 */
class JsonSchemaAnnotationTypeHintStep {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: JsonSchema) {
        if (schema.json is JsonObject) {
            determineType(schema.typeData)?.also { type ->
                schema.json.properties["type"] = JsonTextValue(type)
            }
        }
    }

    private fun determineType(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == JsonTypeHint::class.qualifiedName }
            .map { it.values["type"] as String }
            .firstOrNull()
    }

}