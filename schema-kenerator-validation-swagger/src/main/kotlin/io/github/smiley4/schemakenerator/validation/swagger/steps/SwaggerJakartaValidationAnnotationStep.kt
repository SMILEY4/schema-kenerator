package io.github.smiley4.schemakenerator.validation.swagger.steps

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.AbstractSwaggerSchemaStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties
import io.swagger.v3.oas.models.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

/**
 * Adds support for the following Jakarta Validation annotations:
 * - [NotNull]
 * - [NotEmpty]
 * - [NotBlank]
 * - [Size]
 * - [Min]
 * - [Max]
 */
class SwaggerJakartaValidationAnnotationStep : AbstractSwaggerSchemaStep() {

    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            val mergedAnnotations = propData.annotations + propTypeData.annotations
            getNotNull(mergedAnnotations)?.also { setRequiredNotNull(schema.swagger, propData.name) }
            getNotEmpty(mergedAnnotations)?.also { setRequiredNotNull(schema.swagger, propData.name) }
            getNotBlank(mergedAnnotations)?.also { setRequiredNotNull(schema.swagger, propData.name) }
            getSize(mergedAnnotations)?.also {
                val min = it.values["min"] as Int

                if (min > 0) {
                    prop.minLength = min
                }

                val max = it.values["max"] as Int

                if (max < Int.MAX_VALUE) {
                    prop.maxLength = max
                }
            }
            getMin(mergedAnnotations)?.also { prop.minimum = BigDecimal(it.values["value"] as Long) }
            getMax(mergedAnnotations)?.also { prop.maximum = BigDecimal(it.values["value"] as Long) }
        }
    }

    private fun getNotNull(annotations: Collection<AnnotationData>): AnnotationData? {
        return annotations.findAnnotation<NotNull>()
    }

    private fun getNotEmpty(annotations: Collection<AnnotationData>): AnnotationData? {
        return annotations.findAnnotation<NotEmpty>()
    }

    private fun getNotBlank(annotations: Collection<AnnotationData>): AnnotationData? {
        return annotations.findAnnotation<NotBlank>()
    }

    private fun getSize(annotations: Collection<AnnotationData>): AnnotationData? {
        return annotations.findAnnotation<Size>()
    }

    private fun getMin(annotations: Collection<AnnotationData>): AnnotationData? {
        return annotations.findAnnotation<Min>()
    }

    private fun getMax(annotations: Collection<AnnotationData>): AnnotationData? {
        return annotations.findAnnotation<Max>()
    }

    private inline fun <reified T : Annotation> Collection<AnnotationData>.findAnnotation(): AnnotationData? {
        return this.firstOrNull { it.name == T::class.qualifiedName }
    }

    private fun setRequiredNotNull(swagger: Schema<*>, name: String) {
        if (swagger.required?.contains(name) != true) {
            swagger.addRequiredItem(name)
        }
        swagger.properties[name]?.also { prop ->
            if (prop.nullable == true) {
                prop.nullable = false
            }
            if (prop.types != null && prop.types.contains("null")) {
                prop.types.remove("null")
                if (prop.types.size == 1) {
                    prop.type = prop.types.first()
                    prop.types.clear()
                }
            }
        }
    }

}
