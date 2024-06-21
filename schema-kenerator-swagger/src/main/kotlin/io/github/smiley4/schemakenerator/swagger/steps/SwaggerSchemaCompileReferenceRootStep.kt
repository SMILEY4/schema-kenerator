package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.getRefPath
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.shouldReference

/**
 * Resolves references in prepared swagger-schemas by collecting them in the components-section and referencing them.
 * @param pathType how to reference the type, i.e. which name to use
 */
class SwaggerSchemaCompileReferenceRootStep(private val pathType: RefType = RefType.FULL) {

    private val schemaUtils = SwaggerSchemaUtils()


    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compile(bundle: Bundle<SwaggerSchema>): CompiledSwaggerSchema {
        val result = SwaggerSchemaCompileReferenceStep(pathType).compile(bundle)
        if (shouldReference(result.swagger)) {
            val refPath = getRefPath(pathType, result.typeData, bundle.buildTypeDataMap())
            return CompiledSwaggerSchema(
                typeData = result.typeData,
                swagger = schemaUtils.referenceSchema(refPath, true),
                componentSchemas = buildMap {
                    this.putAll(result.componentSchemas)
                    this[refPath] = result.swagger
                }
            )
        } else {
            return CompiledSwaggerSchema(
                typeData = result.typeData,
                swagger = result.swagger,
                componentSchemas = result.componentSchemas
            )
        }

    }


}
