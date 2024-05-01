package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.SwaggerTypeHint

/**
 * Modifies type from annotation [SwaggerTypeHint]
 * - input: [SwaggerSchema]
 * - output: [SwaggerSchema] with modified type from annotations
 */
class SwaggerSchemaAnnotationTypeHintStep {

    fun process(schemas: Collection<SwaggerSchema>): Collection<SwaggerSchema> {
        return schemas.onEach { process(it) }
    }

    private fun process(schema: SwaggerSchema) {
        determineType(schema.typeData)?.also { type ->
            schema.swagger.type = type
        }
    }

    private fun determineType(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == SwaggerTypeHint::class.qualifiedName }
            .map { it.values["type"] as String }
            .firstOrNull()
    }

}