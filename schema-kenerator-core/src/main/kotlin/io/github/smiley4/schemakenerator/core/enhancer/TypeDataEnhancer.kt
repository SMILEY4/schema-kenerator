package io.github.smiley4.schemakenerator.core.enhancer

import io.github.smiley4.schemakenerator.core.parser.TypeDataContext

interface TypeDataEnhancer {
    fun enhance(context: TypeDataContext)
}