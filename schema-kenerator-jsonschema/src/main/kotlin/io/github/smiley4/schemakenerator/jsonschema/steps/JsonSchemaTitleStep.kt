package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue

/**
 * Adds an automatically determined title to schemas.
 * @param titleBuilder the builder for the title
 */
class JsonSchemaTitleStep(private val titleBuilder: (type: TypeData, types: Map<TypeId, TypeData>) -> String) {

    fun process(bundle: Bundle<JsonSchema>): Bundle<JsonSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            process(schema.data, typeDataMap)
            schema.supporting.forEach { process(it, typeDataMap) }
        }
    }

    private fun process(schema: JsonSchema, typeDataMap: Map<TypeId, TypeData>) {
        if (schema.json is JsonObject && schema.json.properties["title"] == null) {
            schema.json.properties["title"] = JsonTextValue(titleBuilder(schema.typeData, typeDataMap))
        }
    }

}
