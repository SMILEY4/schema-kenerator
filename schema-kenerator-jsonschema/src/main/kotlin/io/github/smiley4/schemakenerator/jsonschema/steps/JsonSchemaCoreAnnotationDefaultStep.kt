package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

/**
 * Adds additional metadata from core annotation [SchemaDefault]
 * - input: [JsonSchema]
 * - output: [JsonSchema] with added information from annotations
 */
class JsonSchemaCoreAnnotationDefaultStep {

    fun process(schemas: Collection<JsonSchema>): Collection<JsonSchema> {
        return schemas.onEach { process(it) }
    }

    private fun process(schema: JsonSchema) {
        if (schema.json is JsonObject && schema.json.properties["default"] == null) {
            determineDefaults(schema.typeData)?.also { default ->
                schema.json.properties["default"] = JsonTextValue(default)
            }
        }
    }

    private fun determineDefaults(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == SchemaDefault::class.qualifiedName }
            .map { it.values["default"] as String }
            .firstOrNull()
    }

}