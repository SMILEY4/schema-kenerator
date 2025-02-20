package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject

object JsonSchemaAnnotationUtils {

    /**
     * iterate over the properties of the given schema as pairs of [JsonObject] and [MemberData].
     */
    fun iterateProperties(
        schema: JsonSchemaData,
        typeDataMap: Map<TypeId, TypeData>,
        action: (property: JsonObject, data: MemberData, type: TypeData) -> Unit
    ) {
        if (schema.json is JsonObject && schema.json.properties.containsKey("properties")) {
            (schema.json.properties["properties"] as JsonObject).properties.forEach { (propKey, prop) ->
                schema.typeData.members.find { it.name == propKey }?.also { memberData ->
                    action(prop as JsonObject, memberData, typeDataMap[memberData.type]!!)
                }
            }
        }
    }

}
