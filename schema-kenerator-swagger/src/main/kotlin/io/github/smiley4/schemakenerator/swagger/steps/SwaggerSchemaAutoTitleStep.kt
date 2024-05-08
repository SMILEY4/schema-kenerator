package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType

/**
 * Adds an automatically determined title to schemas
 * - input: [SwaggerSchema]
 * - output: [SwaggerSchema] with added 'title'
 */
class SwaggerSchemaAutoTitleStep(val type: TitleType = TitleType.FULL) {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: SwaggerSchema) {
        if (schema.swagger.title == null) {
            schema.swagger.title = determineTitle(schema)
        }
    }

    private fun determineTitle(schema: SwaggerSchema): String {
        return when (type) {
            TitleType.FULL -> schema.typeData.id.full()
            TitleType.SIMPLE -> schema.typeData.id.simple()
        }
    }

}