package io.github.smiley4.schemakenerator.swagger.data

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.swagger.v3.oas.models.media.Schema

/**
 * A swagger-schema of a type together with all referenced other schemas in the components or inline in the schema.
 */
class CompiledSwaggerSchema(
    val typeData: BaseTypeData,
    val swagger: Schema<*>,
    val componentSchemas: Map<TypeId, Schema<*>>
)