package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaCompileUtils.copyTypeToTypes
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaCompileUtils.iterate
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaCompileUtils.merge
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaCompileUtils.resolveReferences
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData
import io.swagger.v3.oas.models.media.Schema

internal class SwaggerSchemaCompileInlineStep(private val explicitNullTypes: Boolean) {

    private val schemaUtils = SwaggerSchemaUtils()


    /**
     * Inline all referenced schema
     */
    fun compile(input: IntermediateSwaggerSchemaData): CompiledSwaggerSchemaData {
        copyTypeToTypes(input.entries)
        val root = resolveReferences(input.rootSchema) { refObj ->
            val referencedSchema = input[TypeId(refObj.`$ref`)]
            if (referencedSchema == null) {
                refObj
            } else if(referencedSchema.typeData.id == input.rootId) {
                schemaUtils.referenceSelf()
            } else {
                createInlining(refObj, referencedSchema)
            }
        }
        handleDiscriminatorMappings(root)
        return CompiledSwaggerSchemaData(
            typeData = input.rootTypeData,
            swagger = root,
            componentSchemas = emptyMap()
        )
    }


    /**
     * Create an inline swagger-schema to replace the pending referencing schema.
     * @param refObj the schema containing the reference
     * @param schema the schema referenced by [refObj]
     */
    private fun createInlining(refObj: Schema<*>, schema: SwaggerSchemaData): Schema<*> {
        return merge(refObj, schema.swagger).also {
            if (it.nullable == true && explicitNullTypes) {
                setNullable(it)
            }
            it.nullable = null
        }
    }


    /**
     * Explicitly mark the given schema as a nullable type
     */
    private fun setNullable(schema: Schema<*>) {
        if (schema.types != null) {
            schema.types = setOf("null") + schema.types
        }
        if (schema.anyOf != null && schema.anyOf.isNotEmpty()) {
            schema.anyOf = schema.anyOf + schemaUtils.nullSchema()
        }
        if (schema.oneOf != null && schema.oneOf.isNotEmpty()) {
            schema.oneOf = schema.oneOf + schemaUtils.nullSchema()
        }
    }


    /**
     * Remove discriminator mappings -> not supported by when inlining
     */
    private fun handleDiscriminatorMappings(root: Schema<*>) {
        iterate(root) {
            if (root.discriminator != null) {
                // hint: "compile inline" does not support mapping
                root.discriminator.mapping = null
            }
        }
    }

}
