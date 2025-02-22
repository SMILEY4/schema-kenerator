package io.github.smiley4.schemakenerator.jsonschema.data

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode

class CompiledJsonSchemaData(
    /**
     * the original type data
     */
    val typeData: TypeData,
    /**
     * the root json schema
     */
    val json: JsonNode,
    /**
     * the referenced json schemas
     */
    val definitions: Map<String, JsonNode>
)
