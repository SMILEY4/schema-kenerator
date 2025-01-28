package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.annotations.Deprecated
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

internal class SwaggerSchemaCoreAnnotationDeprecatedStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            process(schema.data, typeDataMap)
            schema.supporting.forEach { process(it, typeDataMap) }
        }
    }

    private fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, TypeData>) {
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
        return if (annotations.any { it.name == kotlin.Deprecated::class.qualifiedName }) {
            true
        } else {
            null
        }
    }

}
