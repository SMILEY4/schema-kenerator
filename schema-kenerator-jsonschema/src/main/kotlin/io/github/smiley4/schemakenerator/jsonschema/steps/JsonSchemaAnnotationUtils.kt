package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.typedata.MemberData
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject

object JsonSchemaAnnotationUtils {

    /**
     * iterate over the properties of the given schema as pairs of [JsonObject] and [MemberData].
     */
    fun iterateProperties(
        schema: JsonSchema,
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
