package io.github.smiley4.schemakenerator.jsonschema.data

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode

/**
 * A json-schema of a type.
 */
class JsonSchema(
    /**
     * the json schema
     */
    val json: JsonNode,
    /**
     * the original type data
     */
    val typeData: BaseTypeData
)