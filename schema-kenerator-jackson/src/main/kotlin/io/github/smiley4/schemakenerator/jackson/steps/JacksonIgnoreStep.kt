package io.github.smiley4.schemakenerator.jackson.steps

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.smiley4.schemakenerator.core.GenericBundleIndependentContentStep
import io.github.smiley4.schemakenerator.core.typedata.MemberData
import io.github.smiley4.schemakenerator.core.typedata.TypeData

/**
 * Adds support for jackson [JsonIgnore]-annotation and removes annotated members
 */
class JacksonIgnoreStep : GenericBundleIndependentContentStep<TypeData>() {

    override fun process(input: TypeData) {
        input.members.removeIf { shouldIgnore(it) }
    }

    private fun shouldIgnore(property: MemberData): Boolean {
        return property.annotations.any { it.name == JsonIgnore::class.qualifiedName }
    }

}
