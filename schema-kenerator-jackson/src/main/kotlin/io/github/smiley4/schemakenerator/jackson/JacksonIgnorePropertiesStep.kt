package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData

internal class JacksonIgnorePropertiesStep {

    fun process(input: Bundle<TypeData>): Bundle<TypeData> {
        return input.also { data ->
            process(data.data)
            data.supporting.forEach { process(it) }
        }
    }

    private fun process(input: TypeData) {
        input.members.removeIf { shouldIgnore(it, input) }
    }

    private fun shouldIgnore(property: MemberData, typeData: TypeData): Boolean {
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
