package io.github.smiley4.schemakenerator.core.steps

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData

/**
 * Renames members of objects according to the given function
 */
class RenamePropertiesStep(private val rename: (name: String) -> String) {

    fun process(bundle: Bundle<BaseTypeData>): Bundle<BaseTypeData> {
        return bundle.also { data ->
            process(data.data)
            data.supporting.forEach { process(it) }
        }
    }

    private fun process(typeData: BaseTypeData) {
        when (typeData) {
            is ObjectTypeData -> process(typeData)
            else -> Unit
        }
    }

    private fun process(typeData: ObjectTypeData) {
        typeData.members.forEach { it.name = rename(it.name) }
    }

}
