package io.github.smiley4.schemakenerator.swagger.module

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerator
import io.swagger.v3.oas.models.media.Schema

interface SwaggerSchemaGeneratorModule {
    fun build(generator: SwaggerSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData): Schema<*>?
    fun enhance(generator: SwaggerSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, node: Schema<*>)
}