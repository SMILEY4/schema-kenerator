package io.github.smiley4.schemakenerator.jsonschema.schema

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.json.JsonNode

class JsonSchemaWithDefinitions(
    val typeId: TypeId,
    val json: JsonNode,
    val definitions: Map<TypeId, JsonNode>
)