package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.annotations.Example
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties
import io.swagger.v3.oas.models.media.Schema

/**
 * Adds example-values from [Example]-annotations.
 */
class SwaggerSchemaCoreAnnotationExamplesStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
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
