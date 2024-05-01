package io.github.smiley4.schemakenerator.jsonschema.schema

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jsonschema.json.JsonNode

class JsonSchema(
    val json: JsonNode,
    val typeData: BaseTypeData
)