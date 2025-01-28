package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonIgnoreType
import io.github.smiley4.schemakenerator.core.GenericBundleStep
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.flattenToMap
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId

/**
 * Adds support for jackson [JsonIgnoreType]-annotation and removes members of the annotated type.
 */
class JacksonIgnoreTypeStep : GenericBundleStep<TypeData, TypeData> {

    override fun process(input: Bundle<TypeData>): Bundle<TypeData> {
        val typeDataEntries = input.flattenToMap()
        return input.also { data ->
            process(data.data, typeDataEntries)
            data.supporting.forEach { process(it, typeDataEntries) }
        }
    }

    private fun process(typeData: TypeData, typeDataEntries: Map<TypeId, TypeData>) {
        typeData.members.removeIf { shouldIgnore(it, typeDataEntries) }
    }

    private fun shouldIgnore(property: MemberData, typeDataEntries: Map<TypeId, TypeData>): Boolean {
        return typeDataEntries[property.type]?.annotations?.any { it.name == JsonIgnoreType::class.qualifiedName } ?: false
    }

}
