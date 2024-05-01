package io.github.smiley4.schemakenerator.jsonschema.data

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode

/**
 * A json-schema of a type together with all referenced other schemas in the definitions or inline in the schema.
 */
class CompiledJsonSchema(
    val typeData: BaseTypeData,
    val json: JsonNode,
    val definitions: Map<TypeId, JsonNode>
)