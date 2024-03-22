package io.github.smiley4.schemakenerator.jsonschema.module

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerator
import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject

interface JsonSchemaGeneratorModule {
    fun build(generator: JsonSchemaGenerator, context: TypeParserContext, typeData: BaseTypeData): JsonObject?
    fun enhance(generator: JsonSchemaGenerator, context: TypeParserContext, typeData: BaseTypeData, node: JsonObject)
}