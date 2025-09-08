package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.TypeData

interface DiscriminatorNameProvider {
    fun getDiscriminatorPropertyName(typeData: TypeData): String?
}
