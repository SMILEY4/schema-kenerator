package io.github.smiley4.schemakenerator.jackson.steps

import com.fasterxml.jackson.annotation.JsonProperty
import old.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import old.ObjectTypeData
import old.PropertyData

/**
 * Adds support for the jackson [JsonProperty]-annotation.
 * Renames annotated members and modifies their nullability according to the specified values.
 */
class JacksonPropertyStep {

    fun process(bundle: Bundle<BaseTypeData>): Bundle<BaseTypeData> {
        return bundle.also { data ->
            process(data.data)
            data.supporting.forEach { process(it) }
        }
    }

    private fun process(typeData: BaseTypeData) {
        when (typeData) {
            is ObjectTypeData -> {
                typeData.members.forEach { process(it) }
            }
            else -> Unit
        }
    }

    private fun process(property: PropertyData) {
        property.name = getName(property)
        property.nullable = isNullable(property)
    }

    private fun getName(property: PropertyData): String {
        return property.annotations
            .find { it.name == JsonProperty::class.qualifiedName }
            ?.let { it.values["value"] as String }
            ?: property.name
    }

    private fun isNullable(property: PropertyData): Boolean {
        return property.annotations
            .find { it.name == JsonProperty::class.qualifiedName }
            ?.let { it.values["required"] as Boolean }
            ?.let { !it }
            ?: property.nullable
    }

}
