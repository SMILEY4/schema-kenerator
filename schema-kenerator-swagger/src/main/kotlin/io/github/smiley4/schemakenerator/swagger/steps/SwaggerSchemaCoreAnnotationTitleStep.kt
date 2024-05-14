package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaTitle
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Adds a title specified by the [SchemaTitle]-annotation
 */
class SwaggerSchemaCoreAnnotationTitleStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
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
