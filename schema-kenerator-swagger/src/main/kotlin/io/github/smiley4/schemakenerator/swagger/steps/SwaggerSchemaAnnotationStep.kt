package io.github.smiley4.schemakenerator.swagger.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.PropertyData
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
class SwaggerSchemaAnnotationStep {

    fun process(bundle: Bundle<SwaggerSchema>): Bundle<SwaggerSchema> {
        return bundle.also { schema ->
            process(schema.data)
            schema.supporting.forEach { process(it) }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun process(schema: SwaggerSchema) {
        getTitle(schema.typeData)?.also { schema.swagger.title = it }
        getDescription(schema.typeData)?.also { schema.swagger.description = it }

        removePropertyIf(schema) { _, data ->
            getHidden(data) ?: false
        }

        iterateProperties(schema) { prop, data ->
            getTitle(data)?.also { prop.title = it }
            getDescription(data)?.also { prop.description = it }
            getName(data)?.also { prop.name = it }
            getAllowableValues(data)?.also {
                it.forEach { entry ->
                    @Suppress("UNCHECKED_CAST")
                    (prop as io.swagger.v3.oas.models.media.Schema<Any>).addEnumItemObject(entry)
                }
            }
            getDefaultValue(data)?.also { prop.setDefault(it) }
            getAccessMode(data)?.also {
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
            getMinLength(data)?.also { prop.minLength = it }
            getMaxLength(data)?.also { prop.maxLength = it }
            getFormat(data)?.also { prop.format = it }
            getMinimum(data)?.also { prop.minimum = it }
            getMaximum(data)?.also { prop.maximum = it }
            isExclusiveMinimum(data)?.also { prop.exclusiveMinimum = it }
            isExclusiveMaximum(data)?.also { prop.exclusiveMaximum = it }
        }
    }

    private fun getTitle(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["title"] as String }
            .map { it.ifBlank { null } }
            .firstOrNull()
    }

    private fun getTitle(property: PropertyData): String? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["title"] as String }
            .map { it.ifBlank { null } }
            .firstOrNull()
    }

    private fun getDescription(typeData: BaseTypeData): String? {
        return typeData.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["description"] as String }
            .map { it.ifBlank { null } }
            .firstOrNull()
    }

    private fun getDescription(property: PropertyData): String? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["description"] as String }
            .map { it.ifBlank { null } }
            .firstOrNull()
    }

    private fun getHidden(property: PropertyData): Boolean? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["hidden"] as Boolean }
            .firstOrNull()
    }

    private fun getName(property: PropertyData): String? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["name"] as String }
            .map { it.ifBlank { null } }
            .firstOrNull()
    }

    private fun getAllowableValues(property: PropertyData): List<String>? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map {
                @Suppress("UNCHECKED_CAST")
                it.values["allowableValues"] as Array<String>
            }
            .map { it.toList().ifEmpty { null } }
            .firstOrNull()
    }

    private fun getDefaultValue(property: PropertyData): String? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["defaultValue"] as String }
            .map { it.ifBlank { null } }
            .firstOrNull()
    }

    private fun getAccessMode(property: PropertyData): Schema.AccessMode? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["accessMode"] as Schema.AccessMode }
            .firstOrNull()
    }

    private fun getMinLength(property: PropertyData): Int? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["minLength"] as Int }
            .firstOrNull()
    }

    private fun getMaxLength(property: PropertyData): Int? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["maxLength"] as Int }
            .firstOrNull()
    }

    private fun getFormat(property: PropertyData): String? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["format"] as String }
            .firstOrNull()
    }

    private fun getMinimum(property: PropertyData): BigDecimal? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["minimum"] as String }
            .map { BigDecimal(it) }
            .firstOrNull()
    }

    private fun isExclusiveMinimum(property: PropertyData): Boolean? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["exclusiveMinimum"] as Boolean }
            .firstOrNull()
    }

    private fun getMaximum(property: PropertyData): BigDecimal? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["maximum"] as String }
            .map { BigDecimal(it) }
            .firstOrNull()
    }

    private fun isExclusiveMaximum(property: PropertyData): Boolean? {
        return property.annotations
            .filter { it.name == Schema::class.qualifiedName }
            .map { it.values["exclusiveMaximum"] as Boolean }
            .firstOrNull()
    }

}
