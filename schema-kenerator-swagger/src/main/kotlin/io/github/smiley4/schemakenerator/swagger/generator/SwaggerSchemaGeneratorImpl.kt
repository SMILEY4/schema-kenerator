package io.github.smiley4.schemakenerator.swagger.generator

import io.github.smiley4.schemakenerator.core.GenericBundleStep
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Generates swagger-schemas from the given type data. All types in the schema are provisionally referenced by the full type-id.
 * Result needs to be "compiled" to get the final swagger-schema.
 */
class SwaggerSchemaGeneratorImpl(private val modules: List<SwaggerSchemaGenerationModule>) : GenericBundleStep<TypeData, SwaggerSchema>, SwaggerSchemaGenerator {

    override fun process(input: Bundle<TypeData>): Bundle<SwaggerSchema> {
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
