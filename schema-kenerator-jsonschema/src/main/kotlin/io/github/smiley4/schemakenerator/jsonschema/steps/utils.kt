package io.github.smiley4.schemakenerator.jsonschema.steps

import old.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import old.TypeId
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema

/**
 * Returns the [BaseTypeData] contained in this [Bundle] of [JsonSchema] as a map with the [TypeId] as key.
 */
fun Bundle<JsonSchema>.buildTypeDataMap(): Map<TypeId, BaseTypeData> {
    val bundle = this
    return buildMap {
        this[bundle.data.typeData.id] = bundle.data.typeData
        bundle.supporting.forEach { schema ->
            this[schema.typeData.id] = schema.typeData
        }
    }
}
