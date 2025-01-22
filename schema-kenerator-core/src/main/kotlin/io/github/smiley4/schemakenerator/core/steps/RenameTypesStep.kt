package io.github.smiley4.schemakenerator.core.steps

import io.github.smiley4.schemakenerator.core.GenericBundleIndependentContentStep
import io.github.smiley4.schemakenerator.core.annotations.Name
import io.github.smiley4.schemakenerator.core.typedata.AnnotationData
import io.github.smiley4.schemakenerator.core.typedata.TypeData

/**
 * Changes the [TypeData.descriptiveName] to the name specified by a [Name]-annotation.
 */
class RenameTypesStep : GenericBundleIndependentContentStep<TypeData>() {

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
