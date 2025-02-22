package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.annotations.Title
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData

internal class SwaggerSchemaCoreAnnotationTitleStep {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        input.entries.forEach { process(it) }
        return input
    }

    private fun process(schema: SwaggerSchemaData) {
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
