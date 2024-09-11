package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

/**
 * Adds an automatically determined title to schemas.
 * @param titleBuilder the builder for the title
 */
class JsonSchemaTitleStep(private val titleBuilder: (type: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String) {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            process(schema.data, typeDataMap)
            schema.supporting.forEach { process(it, typeDataMap) }
        }
    }

    private fun process(schema: JsonSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.json is JsonObject && schema.json.properties["title"] == null) {
            schema.json.properties["title"] = JsonTextValue(titleBuilder(schema.typeData, typeDataMap))
        }
    }

}
