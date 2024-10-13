package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.SwaggerTypeHint

/**
 * Modifies type of swagger-objects with the [SwaggerTypeHint]-annotation.
 */
class SwaggerSchemaAnnotationTypeHintStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        determineType(schema.typeData)?.also { type ->
            schema.swagger.types = setOf(type)
        }
    }

    private fun determineType(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == SwaggerTypeHint::class.qualifiedName }
            .map { it.values["type"] as String }
            .firstOrNull()
    }

}
