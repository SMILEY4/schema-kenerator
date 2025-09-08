package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.TypeData

internal class ConstDiscriminatorNameProvider(private val discriminatorPropertyName: String) : DiscriminatorNameProvider {

    override fun getDiscriminatorPropertyName(typeData: TypeData): String {
        return discriminatorPropertyName
    }

}
