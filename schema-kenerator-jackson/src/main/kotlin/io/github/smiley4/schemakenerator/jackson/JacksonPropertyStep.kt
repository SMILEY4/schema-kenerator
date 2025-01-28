package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData

internal class JacksonPropertyStep {

    fun process(input: Bundle<TypeData>): Bundle<TypeData> {
        return input.also { data ->
            process(data.data)
            data.supporting.forEach { process(it) }
        }
    }

    private fun process(input: TypeData) {
        input.members.forEach { member ->
            member.name = getName(member)
            member.nullable = isNullable(member)
        }
    }

    private fun getName(property: MemberData): String {
        return property.annotations
            .find { it.name == JsonProperty::class.qualifiedName }
            ?.let { it.values["value"] as String }
            ?: property.name
    }

    private fun isNullable(property: MemberData): Boolean {
        return property.annotations
            .find { it.name == JsonProperty::class.qualifiedName }
            ?.let { it.values["required"] as Boolean }
            ?.let { !it }
            ?: property.nullable
    }

}
