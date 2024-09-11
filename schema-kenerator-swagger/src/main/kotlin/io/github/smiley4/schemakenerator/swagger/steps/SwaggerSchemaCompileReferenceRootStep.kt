package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileUtils.shouldReference

/**
 * Resolves references in prepared swagger-schemas by collecting them in the components-section and referencing them.
 * @param pathBuilder builds the path to reference the type, i.e. which "name" to use
 */
class SwaggerSchemaCompileReferenceRootStep(private val pathBuilder: (type: BaseTypeData, types: Map<TypeId, BaseTypeData>) -> String) {

    private val schemaUtils = SwaggerSchemaUtils()


    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compile(bundle: Bundle<SwaggerSchema>): CompiledSwaggerSchema {
        val result = SwaggerSchemaCompileReferenceStep(pathBuilder).compile(bundle)
        if (shouldReference(result.swagger)) {
            val refPath = pathBuilder(result.typeData, bundle.buildTypeDataMap())
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
