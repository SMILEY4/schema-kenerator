package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject

object JsonSchemaAnnotationUtils {

    /**
     * iterate over the properties of the given schema as pairs of [JsonObject] and [PropertyData].
     */
    fun iterateProperties(
        schema: JsonSchema,
        typeDataMap: Map<TypeId, BaseTypeData>,
        action: (property: JsonObject, data: PropertyData, type: BaseTypeData) -> Unit
    ) {
        if (schema.typeData is ObjectTypeData && schema.json is JsonObject && schema.json.properties.containsKey("properties")) {
            (schema.json.properties["properties"] as JsonObject).properties.forEach { (propKey, prop) ->
                schema.typeData.members.find { it.name == propKey }?.also { propertyData ->
                    action(prop as JsonObject, propertyData, typeDataMap[propertyData.type]!!)
                }
            }
        }
    }

}
