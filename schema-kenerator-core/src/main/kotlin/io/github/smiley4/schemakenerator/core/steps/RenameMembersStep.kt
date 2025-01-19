package io.github.smiley4.schemakenerator.core.steps

import io.github.smiley4.schemakenerator.core.GenericBundleIndependentContentStep
import io.github.smiley4.schemakenerator.core.typedata.TypeData

/**
 * Renames members of types according to the given function
 */
class RenameMembersStep(private val rename: (name: String) -> String) : GenericBundleIndependentContentStep<TypeData>() {

    override fun process(input: TypeData) {
        input.members.forEach { it.name = rename(it.name) }
    }

}
