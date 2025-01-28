package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.smiley4.schemakenerator.core.GenericBundleIndependentContentStep
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData

/**
 * Adds support for the jackson [JsonProperty]-annotation.
 * Renames annotated members and modifies their nullability according to the specified values.
 */
class JacksonPropertyStep : GenericBundleIndependentContentStep<TypeData>() {

    override fun process(input: TypeData) {
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
