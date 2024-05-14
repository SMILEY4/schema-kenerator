package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Adds a description from the [SchemaDescription]-annotation.
 */
class SwaggerSchemaCoreAnnotationDescriptionStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: SwaggerSchema) {
        if (schema.swagger.description == null) {
            determineDescription(schema.typeData)?.also { description ->
                schema.swagger.description = description
            }
        }
        iterateProperties(schema) { prop, data ->
            determineDescription(data)?.also { description ->
                prop.description = description
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
