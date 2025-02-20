package io.github.smiley4.schemakenerator.swagger.generator

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.swagger.v3.oas.models.media.Schema

/**
 * Module to use for generating swagger schema of a matching type
 */
interface SwaggerSchemaGenerationModule {

    data class Context(
        val generator: SwaggerSchemaGenerator,
        val typeData: TypeData,
        val knownTypeData: List<TypeData>
    ) {

        fun generate(typeData: TypeData): Schema<*> {
            return generator.generate(typeData, knownTypeData)
        }

    }

    /**
     * @return whether this module applies to the given type.
     */
    fun applies(typeData: TypeData): Boolean

    /**
     * Generate the swagger schema.
     * @return the generated schema
     */
    fun generate(context: Context): Schema<*>

}
