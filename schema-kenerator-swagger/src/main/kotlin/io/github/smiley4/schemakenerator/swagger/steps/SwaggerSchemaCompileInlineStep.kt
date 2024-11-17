package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.copyTypeToTypes
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.iterate
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.merge
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.resolveReferences
import io.swagger.v3.oas.models.media.Schema

/**
 * Resolves references in prepared swagger-schemas by inlining them.
 */
class SwaggerSchemaCompileInlineStep {

    private val schemaUtils = SwaggerSchemaUtils()

    /**
     * Inline all referenced schema
     */
    fun compile(bundle: Bundle<SwaggerSchema>): CompiledSwaggerSchema {
        val schemaList = bundle.flatten()
        copyTypeToTypes(schemaList)
        val root = resolveReferences(bundle.data.swagger) { refObj ->
            val referencedId = TypeId.parse(refObj.`$ref`)
            val referencedSchema = schemaList.find(referencedId)
            if(referencedSchema != null) {
                merge(refObj, referencedSchema.swagger).also {
                    if(it.nullable == true) {
                        it.nullable = null
                        setNullable(it)
                    }
                    if(it.nullable == false) {
                        it.nullable = null
                    }
                }
            } else {
                refObj
            }
        }
        handleDiscriminatorMappings(root)
        return CompiledSwaggerSchema(
            swagger = root,
            typeData = bundle.data.typeData,
            componentSchemas = emptyMap()
        )
    }

    private fun setNullable(schema: Schema<*>) {
        if(schema.types != null) {
            schema.types = setOf("null") + schema.types
        }
        if(schema.anyOf != null && schema.anyOf.isNotEmpty()) {
            schema.anyOf = schema.anyOf + schemaUtils.nullSchema()
        }
        if(schema.oneOf != null && schema.oneOf.isNotEmpty()) {
            schema.oneOf = schema.oneOf + schemaUtils.nullSchema()
        }
    }

    private fun handleDiscriminatorMappings(root: Schema<*>) {
        iterate(root) {
            if(root.discriminator != null) {
                // hint: "inline" does not support mapping
                root.discriminator.mapping = null
            }
        }
    }

    private fun Collection<SwaggerSchema>.find(id: TypeId): SwaggerSchema? {
        return this.find { it.typeData.id == id }
    }

}
