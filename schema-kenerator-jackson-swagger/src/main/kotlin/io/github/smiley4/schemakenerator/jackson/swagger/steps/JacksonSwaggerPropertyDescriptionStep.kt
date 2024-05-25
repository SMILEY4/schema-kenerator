package io.github.smiley4.schemakenerator.jackson.swagger.steps

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Adds a description to properties according to the jackson [JsonPropertyDescription]-annotation.
 */
class JacksonSwaggerPropertyDescriptionStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: SwaggerSchema) {
        iterateProperties(schema) { prop, data ->
            getDescription(data)?.also { description ->
                prop.description = description
            }
        }
    }

    private fun getDescription(typeData: PropertyData): String? {
        return typeData.annotations
            .filter { it.name == JsonPropertyDescription::class.qualifiedName }
            .map { it.values["value"] as String }
            .firstOrNull()
    }

}
