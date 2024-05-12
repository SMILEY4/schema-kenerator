package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

/**
 * Adds default values from [SchemaDefault]-annotation
 */
class JsonSchemaCoreAnnotationDefaultStep {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
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