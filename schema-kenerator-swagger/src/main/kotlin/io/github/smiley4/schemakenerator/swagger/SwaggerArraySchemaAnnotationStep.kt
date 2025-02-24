package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData
import io.swagger.v3.oas.annotations.media.ArraySchema

internal class SwaggerArraySchemaAnnotationStep {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(schema: SwaggerSchemaData, typeDataMap: Map<TypeId, TypeData>) {
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
