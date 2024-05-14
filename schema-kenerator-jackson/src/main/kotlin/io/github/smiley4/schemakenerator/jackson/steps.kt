package io.github.smiley4.schemakenerator.jackson

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jackson.steps.JacksonSubTypeStep
import kotlin.reflect.KType

/**
 * See [JacksonSubTypeStep]
 */
fun KType.collectJacksonSubTypes(typeProcessing: (type: KType) -> Bundle<BaseTypeData>, maxRecursionDepth: Int = 10): Bundle<KType> {
    return JacksonSubTypeStep(
        typeProcessing = typeProcessing,
        maxRecursionDepth = maxRecursionDepth
    ).process(this)
}
