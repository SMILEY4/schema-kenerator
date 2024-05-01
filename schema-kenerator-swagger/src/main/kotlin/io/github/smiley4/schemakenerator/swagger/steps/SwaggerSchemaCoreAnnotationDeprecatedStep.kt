package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Adds additional metadata from core annotation [SchemaDeprecated] and [Deprecated]
 * - input: [SwaggerSchema]
 * - output: [SwaggerSchema] with added information from annotations
 */
class SwaggerSchemaCoreAnnotationDeprecatedStep {

    fun process(schemas: Collection<SwaggerSchema>): List<SwaggerSchema> {
        return schemas.onEach { process(it) }.toList()
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