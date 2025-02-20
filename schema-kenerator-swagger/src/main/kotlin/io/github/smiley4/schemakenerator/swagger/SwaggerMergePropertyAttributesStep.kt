package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData

internal class SwaggerMergePropertyAttributesStep {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        val open = input.entries.toMutableList()
        val content = input.data.toMutableMap()

        while (open.isNotEmpty()) {
            val current = open.removeFirst()
            process(current, content).forEach {
                content[it.typeData.id] = it
                open.add(it)
            }
        }

        return IntermediateSwaggerSchemaData(
            rootId = input.rootId,
            data = content
        )
    }

    private fun process(schema: SwaggerSchemaData, schemas: Map<TypeId, SwaggerSchemaData>): List<SwaggerSchemaData> {
        val resultingSchemas = mutableListOf<SwaggerSchemaData>()
        schema.swagger.properties?.values
            ?.filter { it.`$ref` != null }
            ?.forEach { property ->
                val propertyData = schemas[TypeId(property.`$ref`)]
                if (propertyData != null) {
                    val derivedId = TypeId.create()
                    val derivedPropertyTypeData = propertyData.typeData.copy(derivedId)
                    val derivedPropertySchema = SwaggerSchemaCompileUtils.copy(propertyData.swagger)
                    SwaggerSchemaCompileUtils.mergeInto(property, derivedPropertySchema)
                    resultingSchemas.add(SwaggerSchemaData(derivedPropertySchema, derivedPropertyTypeData))
                    property.`raw$ref`(derivedId.id)
                }
            }
        return resultingSchemas
    }

}
