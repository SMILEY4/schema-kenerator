package io.github.smiley4.schemakenerator.jsonschema.generator

import io.github.smiley4.schemakenerator.core.GenericBundleStep
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.flatten
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema

class JsonSchemaGeneratorImpl(private val modules: List<JsonSchemaGeneratorModule>) : GenericBundleStep<TypeData, JsonSchema>, JsonSchemaGenerator {

    override fun process(input: Bundle<TypeData>): Bundle<JsonSchema> {
        val typeDataList = input.flatten()
        return Bundle(
            data = generate(input.data, typeDataList),
            supporting = input.supporting.map { generate(it, typeDataList) }
        )
    }

    override fun generate(typeData: TypeData, knownTypeData: List<TypeData>): JsonSchema {
        val module = modules.firstOrNull { it.applies(typeData) }
            ?: throw IllegalArgumentException("No json generator module matches the given type '${typeData.identifyingName.full}' (${typeData.id}).")

        return module.generate(
            JsonSchemaGeneratorModule.Context(
                generator = this,
                typeData = typeData,
                knownTypeData = knownTypeData,
            )
        )
    }


}