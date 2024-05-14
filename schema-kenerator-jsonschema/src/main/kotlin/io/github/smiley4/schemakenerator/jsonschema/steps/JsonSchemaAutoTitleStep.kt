package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType

/**
 * Adds an automatically determined title to schemas.
 * @param type the type of the title
 */
class JsonSchemaAutoTitleStep(val type: TitleType = TitleType.FULL) {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
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
