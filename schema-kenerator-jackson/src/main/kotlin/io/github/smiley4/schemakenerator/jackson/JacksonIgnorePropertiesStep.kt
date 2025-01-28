package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.github.smiley4.schemakenerator.core.GenericBundleIndependentContentStep
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData

/**
 * Adds support for jackson [JsonIgnoreProperties]-annotation and removes specified members from the annotated types.
 */
class JacksonIgnorePropertiesStep : GenericBundleIndependentContentStep<TypeData>() {

    override fun process(input: TypeData) {
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
