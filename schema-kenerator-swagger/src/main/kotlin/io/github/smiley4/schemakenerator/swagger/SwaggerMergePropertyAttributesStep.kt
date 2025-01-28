package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Merge the attributes of a property into the referenced type.
 */
class SwaggerMergePropertyAttributesStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        val open = bundle.flatten().toMutableList()
        val content = bundle.flatten().toMutableList()

        while (open.isNotEmpty()) {
            val current = open.removeFirst()
            val newSchemas = process(current, content)
            content.addAll(newSchemas)
            open.addAll(newSchemas)
        }

        return Bundle(
            data = bundle.data,
            supporting = content.also { it.remove(bundle.data) },
        )
    }

    private fun process(schema: SwaggerSchema, schemas: List<SwaggerSchema>): List<SwaggerSchema> {
        val resultingSchemas = mutableListOf<SwaggerSchema>()
        schema.swagger.properties?.values
            ?.filter { it.`$ref` != null }
            ?.forEach { property ->
                val propertyData = schemas.find { it.typeData.id.id == property.`$ref` }
                if (propertyData != null) {
                    val derivedId = TypeId.create()
                    val derivedPropertyTypeData = propertyData.typeData.copy(derivedId)
                    val derivedPropertySchema = SwaggerSchemaCompileUtils.copy(propertyData.swagger)
                    SwaggerSchemaCompileUtils.mergeInto(property, derivedPropertySchema)
                    resultingSchemas.add(SwaggerSchema(derivedPropertySchema, derivedPropertyTypeData))
                    property.`raw$ref`(derivedId.id)
                }
            }
        return resultingSchemas
    }

}
