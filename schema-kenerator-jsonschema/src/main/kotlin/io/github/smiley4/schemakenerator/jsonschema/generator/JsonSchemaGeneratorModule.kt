package io.github.smiley4.schemakenerator.jsonschema.generator

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonNode

/**
 * Module to use for generating json schema of a matching type
 */
interface JsonSchemaGeneratorModule {

    data class Context(
        val generator: JsonSchemaGenerator,
        val typeData: TypeData,
        val knownTypeData: List<TypeData>
    ) {

        fun generate(typeData: TypeData): JsonNode {
            return generator.generate(typeData, knownTypeData)
        }

    }


    /**
     * @return whether this module applies to the given type.
     */
    fun applies(typeData: TypeData): Boolean


    /**
     * Generate the json schema.
     * @return the generated schema
     */
    fun generate(context: Context): JsonNode

}
