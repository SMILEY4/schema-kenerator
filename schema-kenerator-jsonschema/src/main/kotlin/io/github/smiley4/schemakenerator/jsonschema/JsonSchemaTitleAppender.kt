package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.json.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchema

class JsonSchemaTitleAppender(val type: TitleType = TitleType.FULL) {

    fun append(schemas: Collection<JsonSchema>): List<JsonSchema> {
        schemas.forEach { append(it) }
        return schemas.toList()
    }

    private fun append(schema: JsonSchema) {
        if (schema.json is JsonObject && schema.json.properties["title"] == null) {
            schema.json.properties["title"] = JsonTextValue(determineTitle(schema))
        }
    }

    private fun determineTitle(schema: JsonSchema): String {
        return when (type) {
            TitleType.FULL -> schema.typeId.full()
            TitleType.SIMPLE -> schema.typeId.simple()
        }
    }


}