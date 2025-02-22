package io.github.smiley4.schemakenerator.jackson

import com.fasterxml.jackson.annotation.JsonIgnoreType
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.core.data.TypeId

internal class JacksonIgnoreTypeStep {

    fun process(input: TypeDataGroup): TypeDataGroup {
        input.typeData.forEach { process(it, input.data)}
        return input
    }

    private fun process(typeData: TypeData, typeDataEntries: Map<TypeId, TypeData>) {
        typeData.members.removeIf { shouldIgnore(it, typeDataEntries) }
    }

    private fun shouldIgnore(property: MemberData, typeDataEntries: Map<TypeId, TypeData>): Boolean {
        return typeDataEntries[property.type]?.annotations?.any { it.name == JsonIgnoreType::class.qualifiedName } ?: false
    }

}
