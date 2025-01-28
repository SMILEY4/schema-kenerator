package io.github.smiley4.schemakenerator.swagger.generator

import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

interface SwaggerSchemaGenerationModule {

    data class Context(
        val generator: SwaggerSchemaGenerator,
        val typeData: TypeData,
        val knownTypeData: List<TypeData>
    ) {

        fun generate(typeData: TypeData): SwaggerSchema {
            return generator.generate(typeData, knownTypeData)
        }

    }

    fun applies(typeData: TypeData): Boolean

    fun generate(context: Context): SwaggerSchema

}