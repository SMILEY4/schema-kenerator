package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject

fun iterateProperties(schema: JsonSchema, action: (property: JsonObject, data: PropertyData) -> Unit) {
    if (schema.typeData is ObjectTypeData && schema.json is JsonObject && schema.json.properties.containsKey("properties")) {
        (schema.json.properties["properties"] as JsonObject).properties.forEach { (propKey, prop) ->
            schema.typeData.members.find { it.name == propKey }?.also { propertyData ->
                action(prop as JsonObject, propertyData)
            }
        }
    }
}