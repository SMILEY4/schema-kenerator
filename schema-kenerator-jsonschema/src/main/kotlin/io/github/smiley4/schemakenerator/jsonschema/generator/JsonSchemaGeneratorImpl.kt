package io.github.smiley4.schemakenerator.jsonschema.generator

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchemaData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode

internal class JsonSchemaGeneratorImpl(private val modules: List<JsonSchemaGeneratorModule>) : JsonSchemaGenerator {

    fun process(input: TypeDataGroup): IntermediateJsonSchemaData {
        return IntermediateJsonSchemaData(
            rootId = input.rootId,
            data = input.typeData
                .map {
                    JsonSchemaData(
                        typeData = it,
                        json = generate(it, input.typeData)
                    )
                }
                .associateBy { it.typeData.id }
        )
    }

    override fun generate(typeData: TypeData, knownTypeData: List<TypeData>): JsonNode {
        val module = modules.firstOrNull { it.applies(typeData) }
            ?: throw IllegalArgumentException("No json generator module matches the given type '${typeData.identifyingName.full}'.")

        return module.generate(
            JsonSchemaGeneratorModule.Context(
                generator = this,
                typeData = typeData,
                knownTypeData = knownTypeData,
            )
        )
    }

}
