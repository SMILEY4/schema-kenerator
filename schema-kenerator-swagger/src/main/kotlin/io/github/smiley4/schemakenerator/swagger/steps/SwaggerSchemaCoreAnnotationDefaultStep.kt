package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Adds default values from [SchemaDefault]-annotation
 */
class SwaggerSchemaCoreAnnotationDefaultStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: SwaggerSchema) {
        if (schema.swagger.default == null) {
            determineDefaults(schema.typeData)?.also { default ->
                schema.swagger.setDefault(default)
            }
        }
    }

    private fun determineDefaults(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == SchemaDefault::class.qualifiedName }
            .map { it.values["default"] as String }
            .firstOrNull()
    }

}