package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.swagger.v3.oas.annotations.media.ArraySchema

internal class SwaggerArraySchemaAnnotationStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        val typeDataMap = bundle.buildTypeDataMap()
        return bundle.also { schema ->
            process(schema.data, typeDataMap)
            schema.supporting.forEach { process(it, typeDataMap) }
        }
    }

    private fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, TypeData>) {
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
            .firstOrNull { it != Int.MAX_VALUE }
    }

    private fun getMaxItems(annotations: Collection<AnnotationData>): Int? {
        return annotations
            .filter { it.name == ArraySchema::class.qualifiedName }
            .map { it.values["maxItems"] as Int }
            .firstOrNull { it != Int.MAX_VALUE }
    }


    private fun getUniqueItems(annotations: Collection<AnnotationData>): Boolean? {
        return annotations
            .filter { it.name == ArraySchema::class.qualifiedName }
            .map { it.values["uniqueItems"] as Boolean }
            .firstOrNull { it }
    }

}
