package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeData

internal class RenameMembersStep(private val rename: (name: String) -> String) {

    fun process(input: Bundle<TypeData>): Bundle<TypeData> {
        return input.also { data ->
            process(data.data)
            data.supporting.forEach { process(it) }
        }
    }

    private fun process(input: TypeData) {
        input.members.forEach { it.name = rename(it.name) }
    }

}
