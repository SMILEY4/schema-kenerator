package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Returns the [BaseTypeData] contained in this [Bundle] of [SwaggerSchema] as a map with the [TypeId] as key.
 */
fun Bundle<SwaggerSchema>.buildTypeDataMap(): Map<TypeId, BaseTypeData> {
    val bundle = this
    return buildMap {
        this[bundle.data.typeData.id] = bundle.data.typeData
        bundle.supporting.forEach { schema ->
            this[schema.typeData.id] = schema.typeData
        }
    }
}