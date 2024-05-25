package io.github.smiley4.schemakenerator.jackson.steps

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData

/**
 * Adds support for jackson [JsonIgnore]-annotation and removes annotated members
 */
class JacksonIgnoreStep {

    fun process(bundle: Bundle<BaseTypeData>): Bundle<BaseTypeData> {
        return bundle.also { data ->
            process(data.data)
            data.supporting.forEach { process(it) }
        }
    }

    private fun process(typeData: BaseTypeData) {
        when (typeData) {
            is ObjectTypeData -> typeData.members.removeIf { shouldIgnore(it) }
            else -> Unit
        }
    }

    private fun shouldIgnore(property: PropertyData): Boolean {
        return property.annotations.any { it.name == JsonIgnore::class.qualifiedName }
    }

}