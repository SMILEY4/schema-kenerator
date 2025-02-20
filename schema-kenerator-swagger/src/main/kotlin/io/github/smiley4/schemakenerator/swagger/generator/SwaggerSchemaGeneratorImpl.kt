package io.github.smiley4.schemakenerator.swagger.generator

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData
import io.swagger.v3.oas.models.media.Schema

internal class SwaggerSchemaGeneratorImpl(private val modules: List<SwaggerSchemaGenerationModule>) : SwaggerSchemaGenerator {

    fun process(input: TypeDataGroup): IntermediateSwaggerSchemaData {
        return IntermediateSwaggerSchemaData(
            rootId = input.rootId,
            data = input.typeData
                .map {
                    SwaggerSchemaData(
                        typeData = it,
                        swagger = generate(it, input.typeData)
                    )
                }
                .associateBy { it.typeData.id }
        )
    }

    override fun generate(typeData: TypeData, typeDataList: List<TypeData>): Schema<*> {
        val module = modules.firstOrNull { it.applies(typeData) }
            ?: throw IllegalArgumentException("No swagger generator module matches the given type '${typeData.identifyingName.full}'.")

        return module.generate(
            SwaggerSchemaGenerationModule.Context(
                generator = this,
                typeData = typeData,
                knownTypeData = typeDataList,
            )
        )
    }

}
