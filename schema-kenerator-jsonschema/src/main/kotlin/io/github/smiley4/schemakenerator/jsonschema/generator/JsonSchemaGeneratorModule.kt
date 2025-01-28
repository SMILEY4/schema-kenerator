package io.github.smiley4.schemakenerator.jsonschema.generator

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema

interface JsonSchemaGeneratorModule {

    data class Context(
        val generator: JsonSchemaGenerator,
        val typeData: TypeData,
        val knownTypeData: List<TypeData>
    ) {

        fun generate(typeData: TypeData): JsonSchema {
            return generator.generate(typeData, knownTypeData)
        }

    }

    fun applies(typeData: TypeData): Boolean

    fun generate(context: Context): JsonSchema

}