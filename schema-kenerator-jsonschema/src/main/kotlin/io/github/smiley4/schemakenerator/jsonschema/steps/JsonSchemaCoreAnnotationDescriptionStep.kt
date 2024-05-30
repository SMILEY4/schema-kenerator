package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationUtils.iterateProperties

/**
 * Adds a description from the [Description]-annotation.
 */
class JsonSchemaCoreAnnotationDescriptionStep {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
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
            .filter { it.name == Description::class.qualifiedName }
            .map { it.values["description"] as String }
            .firstOrNull()
    }

    private fun determineDescription(typeData: PropertyData): String? {
        return typeData.annotations
            .filter { it.name == Description::class.qualifiedName }
            .map { it.values["description"] as String }
            .firstOrNull()
    }

}
