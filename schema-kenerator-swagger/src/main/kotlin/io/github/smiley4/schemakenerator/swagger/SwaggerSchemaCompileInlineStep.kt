package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaCompileUtils.copyTypeToTypes
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaCompileUtils.iterate
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaCompileUtils.merge
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaCompileUtils.resolveReferences
import io.swagger.v3.oas.models.media.Schema

internal class SwaggerSchemaCompileInlineStep {

    private val schemaUtils = SwaggerSchemaUtils()

    /**
     * Inline all referenced schema
     */
    fun compile(bundle: Bundle<SwaggerSchema>): CompiledSwaggerSchema {
        val schemaList = bundle.flatten()
        copyTypeToTypes(schemaList)
        val root = resolveReferences(bundle.data.swagger) { refObj ->
            val referencedSchema = schemaList.find(TypeId(refObj.`$ref`))
            if(referencedSchema == null) {
                refObj
            } else {
                createInlining(refObj, referencedSchema)
            }
        }
        handleDiscriminatorMappings(root)
        return CompiledSwaggerSchema(
            swagger = root,
            typeData = bundle.data.typeData,
            componentSchemas = emptyMap()
        )
    }

    /**
     * Create an inline swagger-schema to replace the pending referencing schema.
     * @param refObj the schema containing the reference
     * @param schema the schema referenced by [refObj]
     */
    private fun createInlining(refObj: Schema<*>, schema: SwaggerSchema): Schema<*> {
        return merge(refObj, schema.swagger).also {
            if(it.nullable == true) {
                it.nullable = null
                setNullable(it)
            }
            if(it.nullable == false) {
                it.nullable = null
            }
        }
    }


    /**
     * Mark the given schema as a nullable type
     */
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


    /**
     * Remove discriminator mappings -> not supported by when inlining
     */
    private fun handleDiscriminatorMappings(root: Schema<*>) {
        iterate(root) {
            if(root.discriminator != null) {
                // hint: "inline" does not support mapping
                root.discriminator.mapping = null
            }
        }
    }

    /**
     * @return the [JsonSchema] for the given [TypeId]
     */
    private fun Collection<SwaggerSchema>.find(id: TypeId): SwaggerSchema? = this.find { it.typeData.id == id }


}
