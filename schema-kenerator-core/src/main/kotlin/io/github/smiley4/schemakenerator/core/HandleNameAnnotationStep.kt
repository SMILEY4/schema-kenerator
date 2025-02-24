package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.annotations.Name
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup

internal class HandleNameAnnotationStep {

    fun process(input: TypeDataGroup): TypeDataGroup {
        input.typeData.forEach { process(it) }
        return input
    }

    private fun process(input: TypeData) {
        input.annotations
            .find { it.name == Name::class.qualifiedName }
            ?.let {
                val name = it.values["name"] as String
                val qualifiedName = it.values["qualifiedName"] as String
                name to (qualifiedName.ifEmpty { null })
            }
            ?.also { (name, qualifiedName) ->
                input.descriptiveName.full = qualifiedName ?: name
                input.descriptiveName.short = name
            }
    }

}
