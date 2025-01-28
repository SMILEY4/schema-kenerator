package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData

internal class JacksonIgnoreStep {

    fun process(input: Bundle<TypeData>): Bundle<TypeData> {
        return input.also { data ->
            process(data.data)
            data.supporting.forEach { process(it) }
        }
    }

    private fun process(input: TypeData) {
        input.members.removeIf { shouldIgnore(it) }
    }

    private fun shouldIgnore(property: MemberData): Boolean {
        return property.annotations.any { it.name == JsonIgnore::class.qualifiedName }
    }

}
