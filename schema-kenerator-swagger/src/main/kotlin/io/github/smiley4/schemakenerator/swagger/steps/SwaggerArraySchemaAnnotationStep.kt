package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties
import io.swagger.v3.oas.annotations.media.ArraySchema

/**
 * Adds support for swagger [ArraySchema]-annotation
 *  - minItems
 *  - maxItems
 *  - uniqueItems
 */
class SwaggerArraySchemaAnnotationStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            val mergedAnnotations = propData.annotations + propTypeData.annotations
            getMinItems(mergedAnnotations)?.also { prop.minItems = it }
            getMaxItems(mergedAnnotations)?.also { prop.maxItems = it }
            getUniqueItems(mergedAnnotations)?.also { prop.uniqueItems = it }
        }
    }

    private fun getMinItems(annotations: Collection<AnnotationData>): Int? {
        return annotations
            .filter { it.name == ArraySchema::class.qualifiedName }
            .map { it.values["minItems"] as Int }
            .firstOrNull()
    }

    private fun getMaxItems(annotations: Collection<AnnotationData>): Int? {
        return annotations
            .filter { it.name == ArraySchema::class.qualifiedName }
            .map { it.values["maxItems"] as Int }
            .firstOrNull()
    }


    private fun getUniqueItems(annotations: Collection<AnnotationData>): Boolean? {
        return annotations
            .filter { it.name == ArraySchema::class.qualifiedName }
            .map { it.values["uniqueItems"] as Boolean }
            .firstOrNull()
    }

}
