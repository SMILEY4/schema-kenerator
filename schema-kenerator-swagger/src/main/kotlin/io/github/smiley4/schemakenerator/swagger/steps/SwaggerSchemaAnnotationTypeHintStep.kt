package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.SwaggerTypeHint

/**
 * Modifies type of swagger-objects with the [SwaggerTypeHint]-annotation.
 */
class SwaggerSchemaAnnotationTypeHintStep {


    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: SwaggerSchema) {
        determineType(schema.typeData)?.also { type ->
            schema.swagger.type = type
        }
    }

    private fun determineType(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == SwaggerTypeHint::class.qualifiedName }
            .map { it.values["type"] as String }
            .firstOrNull()
    }

}