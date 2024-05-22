package io.github.smiley4.schemakenerator.jackson.jsonschema.steps

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationUtils.iterateProperties

class JacksonJsonSchemaAnnotationPropertyDescriptionStep {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: JsonSchema) {
        iterateProperties(schema) { prop, data ->
            determineDescription(data)?.also { description ->
                prop.properties["description"] = JsonTextValue(description)
            }
        }
    }

    private fun determineDescription(typeData: PropertyData): String? {
        return typeData.annotations
            .filter { it.name == JsonPropertyDescription::class.qualifiedName }
            .map { it.values["value"] as String }
            .firstOrNull()
    }

}