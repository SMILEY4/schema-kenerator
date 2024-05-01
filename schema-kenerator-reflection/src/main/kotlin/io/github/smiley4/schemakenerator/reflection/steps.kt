package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionAnnotationSubTypeStep
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep
import kotlin.reflect.KType

/**
 * See [ReflectionAnnotationSubTypeStep]
 */
fun Collection<KType>.collectSubTypes(): Collection<KType> {
    return ReflectionAnnotationSubTypeStep().process(this)
}


/**
 * See [ReflectionTypeProcessingStep]
 */
fun Collection<KType>.processReflection(): Collection<BaseTypeData> {
    return ReflectionTypeProcessingStep().process(this)
}


