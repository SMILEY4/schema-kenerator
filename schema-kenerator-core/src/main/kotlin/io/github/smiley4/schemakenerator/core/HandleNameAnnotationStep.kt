package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.annotations.Name
import io.github.smiley4.schemakenerator.core.data.TypeData

/**
 * Changes the [TypeData.descriptiveName] to the name specified by a [Name]-annotation.
 */
class HandleNameAnnotationStep : GenericBundleIndependentContentStep<TypeData>() {

    override fun process(input: TypeData) {
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
