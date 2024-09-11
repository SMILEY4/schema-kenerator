package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Adds an automatically determined title to schemas.
 * @param titleBuilder the builder for the title
 */
class SwaggerSchemaTitleStep(private val titleBuilder: (type: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String) {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            process(schema.data, typeDataMap)
            schema.supporting.forEach { process(it, typeDataMap) }
        }
    }

    private fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.swagger.title == null) {
            schema.swagger.title = titleBuilder(schema.typeData, typeDataMap)
        }
    }

}
