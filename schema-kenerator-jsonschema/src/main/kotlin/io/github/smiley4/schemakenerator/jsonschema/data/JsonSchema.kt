package io.github.smiley4.schemakenerator.jsonschema.data

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode

/**
 * A json-schema of a type. References other schemas by [TypeId]
 */
class JsonSchema(
    val json: JsonNode,
    val typeData: BaseTypeData
)