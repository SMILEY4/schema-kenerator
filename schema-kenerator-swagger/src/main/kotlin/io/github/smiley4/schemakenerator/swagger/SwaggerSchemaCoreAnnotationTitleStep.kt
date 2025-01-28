package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.annotations.Title
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

internal class SwaggerSchemaCoreAnnotationTitleStep {

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

    private fun determineTitle(typeData: TypeData): String? {
        return typeData.annotations
            .filter { it.name == Title::class.qualifiedName }
            .map { it.values["title"] as String }
            .firstOrNull()
    }

}
