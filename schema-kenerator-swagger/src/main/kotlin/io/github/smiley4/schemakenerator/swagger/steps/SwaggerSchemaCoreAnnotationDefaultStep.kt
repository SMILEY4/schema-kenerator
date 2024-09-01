package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.Default
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Adds default values from [Default]-annotation
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
            determineDefaults(schema.typeData.annotations)?.also { default ->
                schema.swagger.setDefault(default)
            }
        }
        iterateProperties(schema) { prop, data ->
            determineDefaults(data.annotations)?.also { default ->
                prop.setDefault(default)
            }
        }
    }

    private fun determineDefaults(annotations: List<AnnotationData>): String? {
        return annotations
            .filter { it.name == Default::class.qualifiedName }
            .map { it.values["default"] as String }
            .firstOrNull()
    }

}
