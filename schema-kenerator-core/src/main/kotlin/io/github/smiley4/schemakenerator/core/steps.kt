package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.steps.ConnectSubTypesStep

/**
 * See [ConnectSubTypesStep]
 */
fun Bundle<BaseTypeData>.connectSubTypes(): Bundle<BaseTypeData> {
    return ConnectSubTypesStep().process(this)
}
