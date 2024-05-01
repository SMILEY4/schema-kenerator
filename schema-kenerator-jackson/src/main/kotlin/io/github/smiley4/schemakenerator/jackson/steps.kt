package io.github.smiley4.schemakenerator.jackson

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.jackson.steps.JacksonSubTypeStep
import kotlin.reflect.KType

/**
 * See [JacksonSubTypeStep]
 */
fun Collection<KType>.collectJacksonSubTypes(
    typeProcessing: (types: Collection<KType>) -> Collection<BaseTypeData>,
    maxRecursionDepth: Int = 10
): Collection<KType> {
    return JacksonSubTypeStep(
        typeProcessing = typeProcessing,
        maxRecursionDepth = maxRecursionDepth
    ).process(this)
}