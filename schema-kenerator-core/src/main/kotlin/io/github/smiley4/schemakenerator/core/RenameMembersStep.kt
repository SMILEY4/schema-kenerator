package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup

internal class RenameMembersStep(private val rename: (name: String) -> String) {

    fun process(input: TypeDataGroup): TypeDataGroup {
        input.typeData.forEach { process(it) }
        return input
    }

    private fun process(input: TypeData) {
        input.members.forEach { it.name = rename(it.name) }
    }

}
