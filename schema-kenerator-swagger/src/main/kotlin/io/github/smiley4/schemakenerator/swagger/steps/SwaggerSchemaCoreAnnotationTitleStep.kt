package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.Title
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Adds a title specified by the [Title]-annotation
 */
class SwaggerSchemaCoreAnnotationTitleStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, TypeData>) {
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
