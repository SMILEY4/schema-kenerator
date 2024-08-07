package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.Optional
import io.github.smiley4.schemakenerator.core.annotations.Required
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Sets properties as optional/required from core [Optional] and [Required]-annotation.
 */
class SwaggerSchemaCoreAnnotationOptionalAndRequiredStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: SwaggerSchema) {
        iterateProperties(schema) { prop, data ->
            determineRequired(data)?.also { required ->
                if (required) {
                    addRequired(schema, data.name)
                } else {
                    removeRequired(schema, data.name)
                }
            }
        }
    }

    private fun determineRequired(typeData: PropertyData): Boolean? {
        if (typeData.annotations.any { it.name == Required::class.qualifiedName }) {
            return true
        }
        if (typeData.annotations.any { it.name == Optional::class.qualifiedName }) {
            return false
        }
        return null
    }

    private fun getRequiredList(schema: SwaggerSchema): MutableList<String> {
        return schema.swagger.required ?: mutableListOf()
    }

    private fun addRequired(schema: SwaggerSchema, propertyName: String) {
        val list = getRequiredList(schema)
        if (list.none { it == propertyName }) {
            list.add(propertyName)
        }
    }

    private fun removeRequired(schema: SwaggerSchema, propertyName: String) {
        val list = getRequiredList(schema)
        list.removeIf { it == propertyName }
    }

}
