package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Adds additional metadata from core annotation [SchemaDefault]
 * - input: [SwaggerSchema]
 * - output: [SwaggerSchema] with added information from annotations
 */
class SwaggerSchemaCoreAnnotationDefaultStep {

    fun process(schemas: Collection<SwaggerSchema>): List<SwaggerSchema> {
        return schemas.onEach { process(it) }.toList()
    }

    private fun process(schema: SwaggerSchema) {
        if (schema.swagger.default == null) {
            determineDefaults(schema.typeData)?.also { default ->
                schema.swagger.setDefault(default)
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