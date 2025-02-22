package io.github.smiley4.schemakenerator.jsonschema.generator

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode

interface JsonSchemaGenerator {
    fun generate(typeData: TypeData, knownTypeData: List<TypeData>): JsonNode
}
