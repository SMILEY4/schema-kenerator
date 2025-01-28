package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.annotations.Example
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.swagger.v3.oas.models.media.Schema

internal class SwaggerSchemaCoreAnnotationExamplesStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            process(schema.data, typeDataMap)
            schema.supporting.forEach { process(it, typeDataMap) }
        }
    }

    private fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, TypeData>) {
        if (schema.swagger.examples == null) {
            determineExamples(schema.typeData.annotations)?.also {
                @Suppress("UNCHECKED_CAST")
                (schema.swagger as Schema<Any?>).example = it
            }
        }
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineExamples(propData.annotations + propTypeData.annotations)?.also {
                @Suppress("UNCHECKED_CAST")
                (prop as Schema<Any?>).example = it
            }
        }
    }

    private fun determineExamples(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == Example::class.qualifiedName }
            .map { it.values["example"] as String }
            .firstOrNull()
    }

}
