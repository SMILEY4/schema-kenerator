package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Sets deprecated-flag from [SchemaDeprecated] or kotlin's [Deprecated]-annotation.
 */
class SwaggerSchemaCoreAnnotationDeprecatedStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: SwaggerSchema) {
        if (schema.swagger.deprecated == null) {
            determineDeprecated(schema.typeData)?.also { deprecated ->
                schema.swagger.deprecated = deprecated
            }
        }
        iterateProperties(schema) { prop, data ->
            determineDeprecated(data)?.also { deprecated ->
                prop.deprecated = deprecated
            }
        }
    }

    private fun determineDeprecated(typeData: BaseTypeData): Boolean? {
        return determineDeprecatedCore(typeData.annotations) ?: determineDeprecatedStd(typeData.annotations)
    }

    private fun determineDeprecated(typeData: PropertyData): Boolean? {
        return determineDeprecatedCore(typeData.annotations) ?: determineDeprecatedStd(typeData.annotations)
    }

    private fun determineDeprecatedCore(annotations: Collection<AnnotationData>): Boolean? {
        return annotations
            .filter { it.name == SchemaDeprecated::class.qualifiedName }
            .map { it.values["deprecated"] as Boolean }
            .firstOrNull()
    }

    private fun determineDeprecatedStd(annotations: Collection<AnnotationData>): Boolean? {
        return if (annotations.any { it.name == Deprecated::class.qualifiedName }) {
            true
        } else {
            null
        }
    }

}