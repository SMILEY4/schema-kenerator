package io.github.smiley4.schemakenerator.swagger.modules

import io.github.smiley4.schemakenerator.swagger.schema.SwaggerSchema


class SwaggerSchemaAutoTitleAppender(val type: TitleType = TitleType.FULL) {

    fun append(schemas: Collection<SwaggerSchema>): List<SwaggerSchema> {
        schemas.forEach { append(it) }
        return schemas.toList()
    }

    private fun append(schema: SwaggerSchema) {
        if (schema.schema.title == null) {
            schema.schema.title = determineTitle(schema)
        }
    }

    private fun determineTitle(schema: SwaggerSchema): String {
        return when (type) {
            TitleType.FULL -> schema.typeData.id.full()
            TitleType.SIMPLE -> schema.typeData.id.simple()
        }
    }

}