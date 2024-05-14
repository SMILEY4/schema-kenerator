package io.github.smiley4.schemakenerator.swagger.data

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.swagger.v3.oas.models.media.Schema

/**
 * A root swagger-schema of a type together with all referenced other schemas in the components or already inlined in the root schema.
 */
class CompiledSwaggerSchema(
    /**
     * the original type data
     */
    val typeData: BaseTypeData,
    /**
     * the root swagger schema
     */
    val swagger: Schema<*>,
    /**
     * the referenced swagger schemas
     */
    val componentSchemas: Map<TypeId, Schema<*>>
)
