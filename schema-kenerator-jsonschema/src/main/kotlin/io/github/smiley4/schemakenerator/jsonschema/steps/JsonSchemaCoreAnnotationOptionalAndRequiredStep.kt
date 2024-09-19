package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.annotations.Optional
import io.github.smiley4.schemakenerator.core.annotations.Required
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAnnotationUtils.iterateProperties

/**
 * Sets properties as optional/required from core [Optional] and [Required]-annotation.
 */
class JsonSchemaCoreAnnotationOptionalAndRequiredStep {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: JsonSchema) {
        iterateProperties(schema) { _, data ->
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

    private fun getRequiredList(schema: JsonSchema): MutableList<JsonNode> {
        val json = schema.json
        if (json is JsonObject) {
            val required = json.properties["required"]
            if (required is JsonArray) {
                return required.items
            }
        }
        return mutableListOf()
    }

    private fun addRequired(schema: JsonSchema, propertyName: String) {
        val list = getRequiredList(schema)
        if (list.none { (it as JsonTextValue).value == propertyName }) {
            list.add(JsonTextValue(propertyName))
        }
    }

    private fun removeRequired(schema: JsonSchema, propertyName: String) {
        val list = getRequiredList(schema)
        list.removeIf { (it as JsonTextValue).value == propertyName }
    }

}
