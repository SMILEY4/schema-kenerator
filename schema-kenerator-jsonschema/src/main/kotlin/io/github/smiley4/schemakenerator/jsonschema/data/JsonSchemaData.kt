package io.github.smiley4.schemakenerator.jsonschema.data

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode

data class JsonSchemaData(
    val json: JsonNode,
    val typeData: TypeData
)
