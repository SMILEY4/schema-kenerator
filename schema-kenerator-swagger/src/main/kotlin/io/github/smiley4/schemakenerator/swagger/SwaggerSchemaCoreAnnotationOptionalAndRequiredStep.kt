package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.annotations.Optional
import io.github.smiley4.schemakenerator.core.annotations.Required
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData

internal class SwaggerSchemaCoreAnnotationOptionalAndRequiredStep  {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: SwaggerSchemaData, typeDataMap: Map<TypeId, TypeData>) {
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

    private fun determineRequired(typeData: MemberData): Boolean? {
        if (typeData.annotations.any { it.name == Required::class.qualifiedName }) {
            return true
        }
        if (typeData.annotations.any { it.name == Optional::class.qualifiedName }) {
            return false
        }
        return null
    }

    private fun getRequiredList(schema: SwaggerSchemaData): MutableList<String> {
        return schema.swagger.required ?: mutableListOf()
    }

    private fun setRequiredList(schema: SwaggerSchemaData, required: List<String>) {
        schema.swagger.required = required
    }

    private fun addRequired(schema: SwaggerSchemaData, propertyName: String) {
        val list = getRequiredList(schema)
        if (list.none { it == propertyName }) {
            list.add(propertyName)
        }
        setRequiredList(schema, list)
    }

    private fun removeRequired(schema: SwaggerSchemaData, propertyName: String) {
        val list = getRequiredList(schema)
        list.removeIf { it == propertyName }
        setRequiredList(schema, list)
    }

}
