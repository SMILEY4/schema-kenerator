package io.github.smiley4.schemakenerator.swagger.data

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.swagger.v3.oas.models.media.Schema

data class SwaggerSchemaData(
    val swagger: Schema<*>,
    val typeData: TypeData
)