package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.annotations.ExclusiveMax
import io.github.smiley4.schemakenerator.core.annotations.ExclusiveMin
import io.github.smiley4.schemakenerator.core.annotations.Max
import io.github.smiley4.schemakenerator.core.annotations.Min
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData
import io.swagger.v3.oas.models.media.Schema
import java.math.BigDecimal

internal class SwaggerSchemaCoreAnnotationMinMaxStep {

    fun process(input: IntermediateSwaggerSchemaData): IntermediateSwaggerSchemaData {
        input.entries.forEach { process(it, input.typeDataById) }
        return input
    }

    private fun process(
        schema: SwaggerSchemaData,
        typeDataMap: Map<TypeId, TypeData>,
    ) {
        process(
            schema,
            typeDataMap,
            { determineMin(it) },
            { schema, value ->
                schema.minimum = BigDecimal.valueOf(value)
                schema.exclusiveMinimumValue = null
            })

        process(
            schema,
            typeDataMap,
            { determineExclusiveMin(it) },
            { schema, value ->
                schema.exclusiveMinimumValue = BigDecimal.valueOf(value)
                schema.minimum = null
            })

        process(
            schema,
            typeDataMap,
            { determineMax(it) },
            { schema, value ->
                schema.maximum = BigDecimal.valueOf(value)
                schema.exclusiveMaximumValue = null
            })

        process(
            schema,
            typeDataMap,
            { determineExclusiveMax(it) },
            { schema, value ->
                schema.exclusiveMaximumValue = BigDecimal.valueOf(value)
                schema.maximum = null
            })

    }

    private fun process(
        schema: SwaggerSchemaData,
        typeDataMap: Map<TypeId, TypeData>,
        determineValue: (annotations: List<AnnotationData>) -> Long?,
        setter: (propSchema: Schema<Any?>, value: Long) -> Unit,
    ) {
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            determineValue(propData.annotations + propTypeData.annotations)?.also {
                @Suppress("UNCHECKED_CAST")
                setter(prop as Schema<Any?>, it)
            }
        }
    }

    private fun determineMin(annotations: Collection<AnnotationData>): Long? {
        return annotations
            .filter { it.name == Min::class.qualifiedName }
            .map { it.values["value"] as Long }
            .firstOrNull()
    }

    private fun determineExclusiveMin(annotations: Collection<AnnotationData>): Long? {
        return annotations
            .filter { it.name == ExclusiveMin::class.qualifiedName }
            .map { it.values["value"] as Long }
            .firstOrNull()
    }

    private fun determineMax(annotations: Collection<AnnotationData>): Long? {
        return annotations
            .filter { it.name == Max::class.qualifiedName }
            .map { it.values["value"] as Long }
            .firstOrNull()
    }

    private fun determineExclusiveMax(annotations: Collection<AnnotationData>): Long? {
        return annotations
            .filter { it.name == ExclusiveMax::class.qualifiedName }
            .map { it.values["value"] as Long }
            .firstOrNull()
    }

}
