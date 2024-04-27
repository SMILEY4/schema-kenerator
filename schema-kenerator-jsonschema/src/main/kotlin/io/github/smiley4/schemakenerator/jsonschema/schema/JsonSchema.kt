package io.github.smiley4.schemakenerator.jsonschema.schema

import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.jsonschema.json.JsonNode

class JsonSchema(
    val json: JsonNode,
    val typeId: TypeId
)