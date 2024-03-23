package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.json.obj

data class JsonSchema(
    val schema: JsonObject,
    val definitions: MutableMap<String, JsonObject> = mutableMapOf()
)

fun JsonSchema.asJson(): JsonObject {
    return this.schema.also {
        if (this.definitions.isNotEmpty()) {
            if (!it.properties.containsKey("definitions")) {
                it.properties["definitions"] = obj { }
            }
            it.properties["definitions"]?.also { jsonDefinitions ->
                this.definitions.forEach { (defName, defSchema) ->
                    (jsonDefinitions as JsonObject).properties[defName] = defSchema
                }
            }
        }
    }
}