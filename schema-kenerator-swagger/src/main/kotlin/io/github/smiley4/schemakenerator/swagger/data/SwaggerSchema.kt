package io.github.smiley4.schemakenerator.swagger.data

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.swagger.v3.oas.models.media.Schema

/**
 * A swagger-schema of a type. References other schemas by [TypeId]
 */
class SwaggerSchema(
    val swagger: Schema<*>,
    val typeData: BaseTypeData
)