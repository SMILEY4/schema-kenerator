package io.github.smiley4.schemakenerator.swagger.schema

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.swagger.v3.oas.models.media.Schema

class SwaggerSchema(
    val schema: Schema<*>,
    val typeData: BaseTypeData
)