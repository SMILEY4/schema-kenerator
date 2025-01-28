package io.github.smiley4.schemakenerator.swagger.generator

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

internal class SwaggerSchemaGeneratorImpl(private val modules: List<SwaggerSchemaGenerationModule>) : SwaggerSchemaGenerator {

    fun process(input: Bundle<TypeData>): Bundle<SwaggerSchema> {
        val allTypeData = listOf(input.data) + input.supporting
        return Bundle(
            data = generate(input.data, allTypeData),
            supporting = input.supporting.map { generate(it, allTypeData) }
        )
    }

    override fun generate(typeData: TypeData, typeDataList: List<TypeData>): SwaggerSchema {
        val module = modules.firstOrNull { it.applies(typeData) }
            ?: throw IllegalArgumentException("No swagger generator module matches the given type '${typeData.identifyingName.full}' (${typeData.id}).")

        return module.generate(
            SwaggerSchemaGenerationModule.Context(
                generator = this,
                typeData = typeData,
                knownTypeData = typeDataList,
            )
        )
    }

}
