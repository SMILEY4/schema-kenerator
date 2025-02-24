package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData

internal class SwaggerSchemaTitleStep(private val titleBuilder: (type: TypeData, types: Map<TypeId, TypeData>) -> String) {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: SwaggerSchemaData, typeDataMap: Map<TypeId, TypeData>) {
        if (schema.swagger.title == null) {
            schema.swagger.title = titleBuilder(schema.typeData, typeDataMap)
        }
    }

}
