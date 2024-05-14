package io.github.smiley4.schemakenerator.swagger.data

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.swagger.v3.oas.models.media.Schema

/**
 * A swagger-schema of a type.
 */
class SwaggerSchema(
    /**
     * the swagger schema
     */
    val swagger: Schema<*>,
    /**
     * the original type data
     */
    val typeData: BaseTypeData
)
