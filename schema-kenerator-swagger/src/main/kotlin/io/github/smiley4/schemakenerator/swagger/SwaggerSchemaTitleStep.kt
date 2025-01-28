package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

internal class SwaggerSchemaTitleStep(private val titleBuilder: (type: TypeData, types: Map<TypeId, TypeData>) -> String) {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            process(schema.data, typeDataMap)
            schema.supporting.forEach { process(it, typeDataMap) }
        }
    }

    private fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, TypeData>) {
        if (schema.swagger.title == null) {
            schema.swagger.title = titleBuilder(schema.typeData, typeDataMap)
        }
    }

}
