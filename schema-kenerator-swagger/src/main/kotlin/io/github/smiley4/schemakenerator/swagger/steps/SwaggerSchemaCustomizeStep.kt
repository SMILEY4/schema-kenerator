package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties
import io.swagger.v3.oas.models.media.Schema

/**
 * Further customization options
 */
class SwaggerSchemaCustomizeStep {

    /**
     * Provide a function that is called for each type and swagger-schema. Can be used to manually manipulate the generated swagger-schema.
     */
    fun customizeTypes(
        bundle: Bundle<SwaggerSchema>,
        action: (typeData: BaseTypeData, typeSchema: Schema<*>) -> Unit
    ): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            processTypes(schema.data, action)
            schema.supporting.forEach { processTypes(it, action) }
        }
    }

    private fun processTypes(schema: SwaggerSchema, action: (typeData: BaseTypeData, typeSchema: Schema<*>) -> Unit) {
        action(schema.typeData, schema.swagger)
    }


    /**
     * Provide a function that is called for each property. Can be used to manually manipulate the generated swagger-schema.
     */
    fun customizeProperties(
        bundle: Bundle<SwaggerSchema>,
        action: (propertyData: PropertyData, propertySchema: Schema<*>) -> Unit
    ): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            processProperties(schema.data, action)
            schema.supporting.forEach { processProperties(it, action) }
        }
    }

    private fun processProperties(schema: SwaggerSchema, action: (typeData: PropertyData, typeSchema: Schema<*>) -> Unit) {
        iterateProperties(schema) { prop, data ->
            action(data, prop)
        }
    }

}
