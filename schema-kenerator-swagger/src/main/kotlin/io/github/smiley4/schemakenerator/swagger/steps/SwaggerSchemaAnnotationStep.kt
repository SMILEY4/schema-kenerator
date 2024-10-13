package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationUtils.removePropertyIf
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

/**
 * Adds support for swagger [Schema]-annotation
 * - on types
 *      - title
 *      - description
 * - on properties
 *      - name
 *      - title
 *      - description
 *      - hidden
 *      - allowableValues
 *      - defaultValue
 *      - accessMode
 *      - minLength
 *      - maxLength,
 *      - format
 *      - minimum
 *      - maximum
 *      - exclusiveMaximum
 *      - exclusiveMinimum
 */
class SwaggerSchemaAnnotationStep : AbstractSwaggerSchemaStep() {

    @Suppress("CyclomaticComplexMethod")
    override fun process(schema: SwaggerSchema, typeDataMap: Map<TypeId, BaseTypeData>) {
        getTitle(schema.typeData.annotations)?.also { schema.swagger.title = it }
        getDescription(schema.typeData.annotations)?.also { schema.swagger.description = it }

        removePropertyIf(schema) { _, data ->
            getHidden(data.annotations) ?: false
        }

        iterateProperties(schema, typeDataMap) { prop, propData, propTypeData ->
            val mergedAnnotations = propData.annotations + propTypeData.annotations
            getTitle(mergedAnnotations)?.also { prop.title = it }
            getDescription(mergedAnnotations)?.also { prop.description = it }
            getName(mergedAnnotations)?.also { prop.name = it }
            getAllowableValues(mergedAnnotations)?.onEach { entry ->
                @Suppress("UNCHECKED_CAST")
                (prop as io.swagger.v3.oas.models.media.Schema<Any>).addEnumItemObject(entry)
            }
            getDefaultValue(mergedAnnotations)?.also { prop.setDefault(it) }
            getAccessMode(mergedAnnotations)?.also {
                when(it) {
                    Schema.AccessMode.AUTO -> Unit
                    Schema.AccessMode.READ_ONLY -> prop.readOnly = true
                    Schema.AccessMode.WRITE_ONLY -> prop.writeOnly = true
                    Schema.AccessMode.READ_WRITE -> {
                        prop.readOnly = false
                        prop.writeOnly = false
                    }
                }
            }
            getMinLength(mergedAnnotations)?.also { prop.minLength = it }
            getMaxLength(mergedAnnotations)?.also { prop.maxLength = it }
            getFormat(mergedAnnotations)?.also { prop.format = it }
            getMinimum(mergedAnnotations)?.also { prop.minimum = it }
            getMaximum(mergedAnnotations)?.also { prop.maximum = it }
            isExclusiveMinimum(mergedAnnotations)?.also { prop.exclusiveMinimum = it }
            isExclusiveMaximum(mergedAnnotations)?.also { prop.exclusiveMaximum = it }
        }
    }

    private fun getTitle(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["title"] as String }
            .map { it.ifBlank { null } }
            .firstOrNull()
    }
    private fun getDescription(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["description"] as String }
            .map { it.ifBlank { null } }
            .firstOrNull()
    }

    private fun getHidden(annotations: Collection<AnnotationData>): Boolean? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["hidden"] as Boolean }
            .firstOrNull()
    }

    private fun getName(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["name"] as String }
            .map { it.ifBlank { null } }
            .firstOrNull()
    }

    private fun getAllowableValues(annotations: Collection<AnnotationData>): List<String>? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map {
                @Suppress("UNCHECKED_CAST")
                it.values["allowableValues"] as Array<String>
            }
            .map { it.toList().ifEmpty { null } }
            .firstOrNull()
    }

    private fun getDefaultValue(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["defaultValue"] as String }
            .map { it.ifBlank { null } }
            .firstOrNull()
    }

    private fun getAccessMode(annotations: Collection<AnnotationData>): Schema.AccessMode? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["accessMode"] as Schema.AccessMode }
            .firstOrNull()
    }

    private fun getMinLength(annotations: Collection<AnnotationData>): Int? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["minLength"] as Int }
            .firstOrNull { it >= 0 }
    }

    private fun getMaxLength(annotations: Collection<AnnotationData>): Int? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["maxLength"] as Int }
            .firstOrNull { it >= 0 }
    }

    private fun getFormat(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["format"] as String }
            .firstOrNull { it.isNotBlank() }
    }

    private fun getMinimum(annotations: Collection<AnnotationData>): BigDecimal? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["minimum"] as String }
            .filter { it.isNotBlank() }
            .map { BigDecimal(it) }
            .firstOrNull()
    }

    private fun isExclusiveMinimum(annotations: Collection<AnnotationData>): Boolean? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["exclusiveMinimum"] as Boolean }
            .firstOrNull()
    }

    private fun getMaximum(annotations: Collection<AnnotationData>): BigDecimal? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["maximum"] as String }
            .filter { it.isNotBlank() }
            .map { BigDecimal(it) }
            .firstOrNull()
    }

    private fun isExclusiveMaximum(annotations: Collection<AnnotationData>): Boolean? {
        return annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["exclusiveMaximum"] as Boolean }
            .firstOrNull()
    }

}
