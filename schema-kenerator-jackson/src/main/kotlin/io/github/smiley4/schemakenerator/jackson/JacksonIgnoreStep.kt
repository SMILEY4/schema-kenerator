package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup

internal class JacksonIgnoreStep {

    fun process(input: TypeDataGroup): TypeDataGroup {
        input.typeData.forEach { process(it) }
        return input
    }

    private fun process(input: TypeData) {
        input.members.removeIf { shouldIgnore(it) }
    }

    private fun shouldIgnore(property: MemberData): Boolean {
        return property.annotations.any { it.name == JsonIgnore::class.qualifiedName }
    }

}
