package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Adds additional metadata from core annotation [SchemaDescription]
 * - input: [SwaggerSchema]
 * - output: [SwaggerSchema] with added information from annotations
 */
class SwaggerSchemaCoreAnnotationDescriptionStep {

    fun process(schemas: Collection<SwaggerSchema>): List<SwaggerSchema> {
        return schemas.onEach { process(it) }.toList()
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