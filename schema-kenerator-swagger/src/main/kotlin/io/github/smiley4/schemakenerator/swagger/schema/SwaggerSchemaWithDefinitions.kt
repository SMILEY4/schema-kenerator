package io.github.smiley4.schemakenerator.swagger.schema

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.swagger.v3.oas.models.media.Schema

class SwaggerSchemaWithDefinitions(
    val typeData: BaseTypeData,
    val schema: Schema<*>,
    val definitions: Map<TypeId, Schema<*>>
)