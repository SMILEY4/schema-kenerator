package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaTitle
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Adds additional metadata from core annotation [SchemaTitle]
 * - input: [SwaggerSchema]
 * - output: [SwaggerSchema] with added information from annotations
 */
class SwaggerSchemaCoreAnnotationTitleStep {

    fun process(schemas: Collection<SwaggerSchema>): List<SwaggerSchema> {
        return schemas.onEach { process(it) }.toList()
    }

    private fun process(schema: SwaggerSchema) {
        if (schema.swagger.title == null) {
            determineTitle(schema.typeData)?.also { title ->
                schema.swagger.title = title
            }
        }
    }

    private fun determineTitle(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == SchemaTitle::class.qualifiedName }
            .map { it.values["title"] as String }
            .firstOrNull()
    }

}