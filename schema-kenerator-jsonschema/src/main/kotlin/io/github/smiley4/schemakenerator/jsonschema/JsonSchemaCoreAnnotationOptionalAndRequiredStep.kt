package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.annotations.Optional
import io.github.smiley4.schemakenerator.core.annotations.Required
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonArray
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

internal class JsonSchemaCoreAnnotationOptionalAndRequiredStep {

    fun process(input: IntermediateJsonSchemaData): IntermediateJsonSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: JsonSchemaData, typeDataMap: Map<TypeId, TypeData>) {
        iterateProperties(schema, typeDataMap) { _, data, _ ->
            determineRequired(data)?.also { required ->
                if (required) {
                    addRequired(schema, data.name)
                } else {
                    removeRequired(schema, data.name)
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

    private fun getRequiredList(schema: JsonSchemaData): MutableList<JsonNode> {
        val json = schema.json
        if (json is JsonObject) {
            val required = json.properties["required"]
            if (required is JsonArray) {
                return required.items
            }
        }
        return mutableListOf()
    }

    private fun addRequired(schema: JsonSchemaData, propertyName: String) {
        val list = getRequiredList(schema)
        if (list.none { (it as JsonTextValue).value == propertyName }) {
            list.add(JsonTextValue(propertyName))
        }
    }

    private fun removeRequired(schema: JsonSchemaData, propertyName: String) {
        val list = getRequiredList(schema)
        list.removeIf { (it as JsonTextValue).value == propertyName }
    }

}
