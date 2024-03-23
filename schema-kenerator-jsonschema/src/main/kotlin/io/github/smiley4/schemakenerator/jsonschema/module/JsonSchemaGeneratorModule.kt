package io.github.smiley4.schemakenerator.jsonschema.module

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.jsonschema.JsonSchema
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerator

interface JsonSchemaGeneratorModule {
    fun build(generator: JsonSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, depth: Int): JsonSchema?
    fun enhance(generator: JsonSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, schema: JsonSchema, depth: Int)
}