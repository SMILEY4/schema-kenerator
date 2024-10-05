package io.github.smiley4.schemakenerator.jsonschema.data

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode

/**
 * A root json-schema of a type together with all referenced other schemas in the definitions or already inlined in the root schema.
 */
class CompiledJsonSchema(
    /**
     * the original type data
     */
    val typeData: BaseTypeData,
    /**
     * the root json schema
     */
    val json: JsonNode,
    /**
     * the referenced json schemas
     */
    val definitions: Map<String, JsonNode>
)
