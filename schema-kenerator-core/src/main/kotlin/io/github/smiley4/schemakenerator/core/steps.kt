package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.steps.ConnectSubTypesStep
import io.github.smiley4.schemakenerator.core.steps.MergeGettersStep
import io.github.smiley4.schemakenerator.core.steps.RenameTypesStep

/**
 * See [ConnectSubTypesStep]
 */
fun Bundle<BaseTypeData>.connectSubTypes(): Bundle<BaseTypeData> {
    return ConnectSubTypesStep().process(this)
}

/**
 * See [RenameTypesStep]
 */
fun Bundle<BaseTypeData>.renameTypes(): Bundle<BaseTypeData> {
    return RenameTypesStep().process(this)
}


/**
 * See [MergeGettersStep]
 */
fun Bundle<BaseTypeData>.mergeGetters(): Bundle<BaseTypeData> {
    return MergeGettersStep().process(this)
}
