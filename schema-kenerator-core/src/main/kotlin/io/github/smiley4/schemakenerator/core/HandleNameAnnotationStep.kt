package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.annotations.Name
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.TypeData

internal class HandleNameAnnotationStep {

    fun process(input: Bundle<TypeData>): Bundle<TypeData> {
        return input.also { data ->
            process(data.data)
            data.supporting.forEach { process(it) }
        }
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
