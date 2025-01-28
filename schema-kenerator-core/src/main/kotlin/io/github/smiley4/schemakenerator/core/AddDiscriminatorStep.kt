package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.TypeData

internal class AddDiscriminatorStep(private val discriminatorPropertyName: String) : AbstractAddDiscriminatorStep() {

    override fun getDiscriminatorPropertyName(typeData: TypeData): String {
        return discriminatorPropertyName
    }

}
