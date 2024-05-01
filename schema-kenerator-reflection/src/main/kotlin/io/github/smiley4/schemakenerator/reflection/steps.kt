package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionAnnotationSubTypeStep
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep.Companion.DEFAULT_PRIMITIVE_TYPES
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * See [ReflectionAnnotationSubTypeStep]
 */
fun Collection<KType>.collectSubTypes(maxRecursionDepth: Int = 10): Collection<KType> {
    return ReflectionAnnotationSubTypeStep(
        maxRecursionDepth = maxRecursionDepth
    ).process(this)
}


class ReflectionTypeProcessingStepConfig {
    internal var customProcessors = mutableMapOf<KClass<*>, () -> BaseTypeData>()
    var includeGetters: Boolean = false
    var includeWeakGetters: Boolean = false
    var includeFunctions: Boolean = false
    var includeHidden: Boolean = false
    var includeStatic: Boolean = false
    var primitiveTypes: Collection<KClass<*>> = DEFAULT_PRIMITIVE_TYPES

    fun customProcessor(type: KClass<*>, processor: () -> BaseTypeData) {
        customProcessors[type] = processor
    }

    fun customProcessors(processors: Map<KClass<*>, () -> BaseTypeData>) {
        customProcessors.putAll(processors)
    }
}


/**
 * See [ReflectionTypeProcessingStep]
 */
fun Collection<KType>.processReflection(configBlock: ReflectionTypeProcessingStepConfig.() -> Unit = {}): Collection<BaseTypeData> {
    val config = ReflectionTypeProcessingStepConfig().apply(configBlock)
    return ReflectionTypeProcessingStep(
        includeGetters = config.includeGetters,
        includeWeakGetters = config.includeWeakGetters,
        includeFunctions = config.includeFunctions,
        includeHidden = config.includeHidden,
        includeStatic = config.includeStatic,
        primitiveTypes = config.primitiveTypes,
        customProcessors = config.customProcessors,
    ).process(this)
}


