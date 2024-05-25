package io.github.smiley4.schemakenerator.jackson.steps

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData

/**
 * Adds support for jackson [JsonIgnoreProperties]-annotation and removes specified members from the annotated types.
 */
class JacksonIgnorePropertiesStep {

    fun process(bundle: Bundle<BaseTypeData>): Bundle<BaseTypeData> {
        return bundle.also { data ->
            process(data.data)
            data.supporting.forEach { process(it) }
        }
    }

    private fun process(typeData: BaseTypeData) {
        when (typeData) {
            is ObjectTypeData -> typeData.members.removeIf { shouldIgnore(it, typeData) }
            else -> Unit
        }
    }

    private fun shouldIgnore(property: PropertyData, typeData: BaseTypeData): Boolean {
        val ignoredProperties = typeData.annotations
            .find { it.name == JsonIgnoreProperties::class.qualifiedName }
            ?.let { it.values["value"] }
            ?.let {
                @Suppress("UNCHECKED_CAST")
                (it as Array<String>).toSet()
            }
            ?: emptySet()
        return ignoredProperties.contains(property.name)
    }

}
