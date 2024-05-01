package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType

/**
 * Adds an automatically determined title to schemas
 * - input: [JsonSchema]
 * - output: [JsonSchema] with added 'title'
 */
class JsonSchemaAutoTitleStep(val type: TitleType = TitleType.FULL) {

    fun process(schemas: Collection<JsonSchema>): Collection<JsonSchema> {
        return schemas.onEach { process(it) }
    }

    private fun process(schema: JsonSchema) {
        if (schema.json is JsonObject && schema.json.properties["title"] == null) {
            schema.json.properties["title"] = JsonTextValue(determineTitle(schema))
        }
    }

    private fun determineTitle(schema: JsonSchema): String {
        return when (type) {
            TitleType.FULL -> schema.typeData.id.full()
            TitleType.SIMPLE -> schema.typeData.id.simple()
        }
    }

}