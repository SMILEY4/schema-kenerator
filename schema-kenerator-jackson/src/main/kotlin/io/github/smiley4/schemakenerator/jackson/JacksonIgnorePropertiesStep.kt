package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup

internal class JacksonIgnorePropertiesStep {

    fun process(input: TypeDataGroup): TypeDataGroup {
        input.typeData.forEach { process(it) }
        return input
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
