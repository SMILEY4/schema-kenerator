package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties
import io.swagger.v3.oas.annotations.media.ArraySchema

/**
 * Adds support for swagger [ArraySchema]-annotation
 *  - minItems
 *  - maxItems
 *  - uniqueItems
 */
class SwaggerArraySchemaAnnotationStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: SwaggerSchema) {
        iterateProperties(schema) { prop, data ->
            getMinItems(data)?.also { prop.minItems = it }
            getMaxItems(data)?.also { prop.maxItems = it }
            getUniqueItems(data)?.also { prop.uniqueItems = it }
        }
    }

    private fun getMinItems(property: PropertyData): Int? {
        return property.annotations
            .filter { it.name == ArraySchema::class.qualifiedName }
            .map { it.values["minItems"] as Int }
            .firstOrNull()
    }

    private fun getMaxItems(property: PropertyData): Int? {
        return property.annotations
            .filter { it.name == ArraySchema::class.qualifiedName }
            .map { it.values["maxItems"] as Int }
            .firstOrNull()
    }


    private fun getUniqueItems(property: PropertyData): Boolean? {
        return property.annotations
            .filter { it.name == ArraySchema::class.qualifiedName }
            .map { it.values["uniqueItems"] as Boolean }
            .firstOrNull()
    }

}
