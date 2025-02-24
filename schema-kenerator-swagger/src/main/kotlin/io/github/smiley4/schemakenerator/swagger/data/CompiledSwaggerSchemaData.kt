package io.github.smiley4.schemakenerator.swagger.data

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.swagger.v3.oas.models.media.Schema

class CompiledSwaggerSchemaData(
    /**
     * the original type data
     */
    val typeData: TypeData,
    /**
     * the root swagger schema
     */
    val swagger: Schema<*>,
    /**
     * the referenced swagger schemas
     */
    val componentSchemas: Map<String, Schema<*>>
)
