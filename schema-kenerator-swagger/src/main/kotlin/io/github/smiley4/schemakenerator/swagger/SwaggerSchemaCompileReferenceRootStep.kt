package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaCompileUtils.shouldReference
import io.github.smiley4.schemakenerator.swagger.data.CompiledSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData

internal class SwaggerSchemaCompileReferenceRootStep(
    private val explicitNullTypes: Boolean,
    private val pathBuilder: (type: TypeData, types: Map<TypeId, TypeData>) -> String
) {

    private val schemaUtils = SwaggerSchemaUtils()

    /**
     * Put referenced schemas into definitions and reference them
     */
    fun compile(input: IntermediateSwaggerSchemaData): CompiledSwaggerSchemaData {
        val result = SwaggerSchemaCompileReferenceStep(explicitNullTypes, pathBuilder).compile(input)
        if (shouldReference(result.swagger)) {
            val refPath = pathBuilder(result.typeData, input.typeDataById)
            return CompiledSwaggerSchemaData(
                typeData = result.typeData,
                swagger = schemaUtils.referenceSchema(refPath, true),
                componentSchemas = buildMap {
                    this.putAll(result.componentSchemas)
                    this[refPath] = result.swagger
                }
            )
        } else {
            return CompiledSwaggerSchemaData(
                typeData = result.typeData,
                swagger = result.swagger,
                componentSchemas = result.componentSchemas
            )
        }
    }

}
