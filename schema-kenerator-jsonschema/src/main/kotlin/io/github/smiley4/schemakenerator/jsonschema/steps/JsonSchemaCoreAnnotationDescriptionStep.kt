package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

/**
 * Adds additional metadata from (core) annotation [SchemaDescription]
 * - input: [JsonSchema]
 * - output: [JsonSchema] with added information from annotations
 */
class JsonSchemaCoreAnnotationDescriptionStep {

    fun process(schemas: Collection<JsonSchema>): Collection<JsonSchema> {
        return schemas.onEach { process(it) }
    }

    private fun process(schema: JsonSchema) {
        if (schema.json is JsonObject && schema.json.properties["description"] == null) {
            determineDescription(schema.typeData)?.also { description ->
                schema.json.properties["description"] = JsonTextValue(description)
            }
        }
        iterateProperties(schema) { prop, data ->
            determineDescription(data)?.also { description ->
                prop.properties["description"] = JsonTextValue(description)
            }
        }
    }

    private fun determineDescription(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == SchemaDescription::class.qualifiedName }
            .map { it.values["description"] as String }
            .firstOrNull()
    }

    private fun determineDescription(typeData: PropertyData): String? {
        return typeData.annotations
            .filter { it.name == SchemaDescription::class.qualifiedName }
            .map { it.values["description"] as String }
            .firstOrNull()
    }

}