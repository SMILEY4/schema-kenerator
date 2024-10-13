package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.Optional
import io.github.smiley4.schemakenerator.core.annotations.Required
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Sets properties as optional/required from core [Optional] and [Required]-annotation.
 */
class SwaggerSchemaCoreAnnotationOptionalAndRequiredStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        iterateProperties(schema, typeDataMap) { _, propData, _ ->
            determineRequired(propData)?.also { required ->
                if (required) {
                    addRequired(schema, propData.name)
                } else {
                    removeRequired(schema, propData.name)
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

    private fun setRequiredList(schema: SwaggerSchema, required: List<String>) {
        schema.swagger.required = required
    }

    private fun addRequired(schema: SwaggerSchema, propertyName: String) {
        val list = getRequiredList(schema)
        if (list.none { it == propertyName }) {
            list.add(propertyName)
        }
        setRequiredList(schema, list)
    }

    private fun removeRequired(schema: SwaggerSchema, propertyName: String) {
        val list = getRequiredList(schema)
        list.removeIf { it == propertyName }
        setRequiredList(schema, list)
    }

}
