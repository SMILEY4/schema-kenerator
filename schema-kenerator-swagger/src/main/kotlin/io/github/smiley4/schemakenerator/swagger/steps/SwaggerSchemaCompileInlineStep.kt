package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.merge
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.resolveReferences

/**
 * Resolves references in prepared swagger-schemas by inlining them.
 */
class SwaggerSchemaCompileInlineStep {

    /**
     * Inline all referenced schema
     */
    fun compile(bundle: Bundle<SwaggerSchema>): CompiledSwaggerSchema {
        val schemaList = bundle.flatten()
        val root = resolveReferences(bundle.data.swagger) { refObj ->
            val referencedId = TypeId.parse(refObj.`$ref`)
            val referencedSchema = schemaList.find(referencedId)
            if(referencedSchema != null) {
                merge(refObj, referencedSchema.swagger)
            } else {
                refObj
            }
        }
        return CompiledSwaggerSchema(
            swagger = root,
            typeData = bundle.data.typeData,
            componentSchemas = emptyMap()
        )
    }

    private fun Collection<SwaggerSchema>.find(id: TypeId): SwaggerSchema? {
        return this.find { it.typeData.id == id }
    }

}
