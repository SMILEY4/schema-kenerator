package io.github.smiley4.schemakenerator.jsonschema.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema

/**
 * Returns the [TypeData] contained in this [Bundle] of [JsonSchema] as a map with the [TypeId] as key.
 */
fun Bundle<JsonSchema>.buildTypeDataMap(): Map<TypeId, TypeData> {
    val bundle = this
    return buildMap {
        this[bundle.data.typeData.id] = bundle.data.typeData
        bundle.supporting.forEach { schema ->
            this[schema.typeData.id] = schema.typeData
        }
    }
}
