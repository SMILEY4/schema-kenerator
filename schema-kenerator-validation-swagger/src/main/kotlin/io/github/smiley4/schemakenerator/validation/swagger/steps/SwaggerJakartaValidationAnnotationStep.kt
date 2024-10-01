package io.github.smiley4.schemakenerator.validation.swagger.steps

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils
import jakarta.validation.constraints.*
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
class SwaggerJakartaValidationAnnotationStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    private fun process(schema: SwaggerSchema) {
        SwaggerSchemaAnnotationUtils.iterateProperties(schema) { prop, data ->
            getNotNull(data)?.also { schema.swagger.addRequiredItem(data.name) }
            getNotEmpty(data)?.also { schema.swagger.addRequiredItem(data.name) }
            getNotBlank(data)?.also { schema.swagger.addRequiredItem(data.name) }
            getSize(data)?.also {
                val min = it.values["min"] as Int

                if (min > 0) {
                    prop.minLength = min
                }

                val max = it.values["max"] as Int

                if (max < Int.MAX_VALUE) {
                    prop.maxLength = max
                }
            }
            getMin(data)?.also { prop.minimum = BigDecimal(it.values["value"] as Long) }
            getMax(data)?.also { prop.maximum = BigDecimal(it.values["value"] as Long) }
        }
    }

    private fun getNotNull(property: PropertyData): AnnotationData? {
        return property.findAnnotation<NotNull>()
    }

    private fun getNotEmpty(property: PropertyData): AnnotationData? {
        return property.findAnnotation<NotEmpty>()
    }

    private fun getNotBlank(property: PropertyData): AnnotationData? {
        return property.findAnnotation<NotBlank>()
    }

    private fun getSize(property: PropertyData): AnnotationData? {
        return property.findAnnotation<Size>()
    }

    private fun getMin(property: PropertyData): AnnotationData? {
        return property.findAnnotation<Min>()
    }

    private fun getMax(property: PropertyData): AnnotationData? {
        return property.findAnnotation<Max>()
    }

    private inline fun <reified T : Annotation> PropertyData.findAnnotation(): AnnotationData? {
        return annotations.firstOrNull { it.name == T::class.qualifiedName }
    }

}
