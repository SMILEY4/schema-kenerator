package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.steps.ConnectSubTypesStep

/**
 * See [ConnectSubTypesStep]
 */
fun Collection<BaseTypeData>.connectSubTypes(): List<BaseTypeData> {
    return ConnectSubTypesStep().process(this)
}