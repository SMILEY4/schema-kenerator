package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.Deprecated
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties

/**
 * Sets deprecated-flag from [Deprecated] or kotlin's [Deprecated]-annotation.
 */
class SwaggerSchemaCoreAnnotationDeprecatedStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        if (schema.swagger.deprecated == null) {
            determineDeprecated(schema.typeData.annotations)?.also { deprecated ->
                schema.swagger.deprecated = deprecated
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineDeprecated(propData.annotations + propTypeData.annotations)?.also { deprecated ->
                prop.deprecated = deprecated
            }
        }
    }

    private fun determineDeprecated(annotations: Collection<AnnotationData>): Boolean? {
        return determineDeprecatedCore(annotations) ?: determineDeprecatedStd(annotations)
    }

    private fun determineDeprecatedCore(annotations: Collection<AnnotationData>): Boolean? {
        return annotations
            .filter { it.name == Deprecated::class.qualifiedName }
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
