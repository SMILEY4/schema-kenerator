package io.github.smiley4.schemakenerator.jackson.steps

import com.fasterxml.jackson.annotation.JsonIgnoreType
import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PropertyData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.flattenToMap

/**
 * Adds support for jackson [JsonIgnoreType]-annotation and removes members of the annotated type.
 */
class JacksonIgnoreTypeStep {

    fun process(bundle: Bundle<BaseTypeData>): Bundle<BaseTypeData> {
        val typeDataEntries = bundle.flattenToMap()
        return bundle.also { data ->
            process(data.data, typeDataEntries)
            data.supporting.forEach { process(it, typeDataEntries) }
        }
    }

    private fun process(typeData: BaseTypeData, typeDataEntries: Map<TypeId, BaseTypeData>) {
        when (typeData) {
            is ObjectTypeData -> typeData.members.removeIf { shouldIgnore(it, typeDataEntries) }
            else -> Unit
        }
    }

    private fun shouldIgnore(property: PropertyData, typeDataEntries: Map<TypeId, BaseTypeData>): Boolean {
        return typeDataEntries[property.type]?.annotations?.any { it.name == JsonIgnoreType::class.qualifiedName } ?: false
    }

}