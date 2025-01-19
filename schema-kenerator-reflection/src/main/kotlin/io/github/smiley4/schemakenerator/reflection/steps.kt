package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.data.mapToInputType
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.reflection.data.EnumConstType
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionCustomProcessor
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeMatcher
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionAnnotationSubTypeStep
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep.Companion.DEFAULT_PRIMITIVE_TYPES
import old.PrimitiveTypeData
import old.PropertyType
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * See [ReflectionAnnotationSubTypeStep]
 */
fun KType.collectSubTypes(maxRecursionDepth: Int = 10): Bundle<InputType> {
    return KTypeInput(this).collectSubTypes(maxRecursionDepth)
}


/**
 * See [ReflectionAnnotationSubTypeStep]
 */
fun InputType.collectSubTypes(maxRecursionDepth: Int = 10): Bundle<InputType> {
    return ReflectionAnnotationSubTypeStep(
        maxRecursionDepth = maxRecursionDepth
    ).process(this)
}


class ReflectionTypeProcessingStepConfig {

    var customProcessors = mutableListOf<Pair<ReflectionTypeMatcher, ReflectionCustomProcessor>>()

    var typeRedirects = mutableMapOf<KType, KType>().also { it.putAll(ReflectionTypeProcessingStep.DEFAULT_REDIRECTS) }


    /**
     * Whether to include getters as members of classes (see [PropertyType.GETTER]).
     */
    var includeGetters: Boolean = false


    /**
     * Whether to include weak getters as members of classes (see [PropertyType.WEAK_GETTER]).
     */
    var includeWeakGetters: Boolean = false


    /**
     * Whether to include functions as members of classes (see [PropertyType.FUNCTION]).
     */
    var includeFunctions: Boolean = false


    /**
     * Whether to include hidden (e.g. private) members
     */
    var includeHidden: Boolean = false


    /**
     * Whether to include static members
     */
    var includeStatic: Boolean = false


    /**
     * The list of types that are considered "primitive types" and returned as [PrimitiveTypeData]
     */
    var primitiveTypes: MutableSet<KClass<*>> = DEFAULT_PRIMITIVE_TYPES.toMutableSet()


    /**
     * Whether to use toString for enum values or the declared name
     */
    var enumConstType: EnumConstType = EnumConstType.NAME


    /**
     * Add a custom processor for the given type that overwrites the default behaviour
     */
    fun customProcessor(clazz: KClass<*>, processor: ReflectionCustomProcessor) {
        customProcessors.add(
            { _: KType, c: KClass<*> -> c == clazz } to processor
        )
    }


    /**
     * Add a custom processor for the given type that overwrites the default behaviour
     */
    inline fun <reified T> customProcessor(noinline processor: ReflectionCustomProcessor) {
        customProcessor(typeOf<T>().classifier!! as KClass<*>, processor)
    }


    /**
     * Redirect from the given type to the other given type, i.e. when the "from" type is processed, the "to" type is used instead.
     */
    fun redirect(from: KType, to: KType) {
        typeRedirects[from] = to
    }


    /**
     * Redirect from the given type to the other given type, i.e. when the "from" type is processed, the "to" type is used instead.
     */
    inline fun <reified FROM, reified TO> redirect() {
        redirect(typeOf<FROM>(), typeOf<TO>())
    }


    /**
     * Redirect from the given types to the other given types, i.e. when a type is processed, the associated type is used instead.
     */
    fun redirect(redirects: Map<KType, KType>) {
        typeRedirects.putAll(redirects)
    }
}


/**
 * See [ReflectionTypeProcessingStep]
 */
fun KType.processReflection(configBlock: ReflectionTypeProcessingStepConfig.() -> Unit = {}): Bundle<TypeData> {
    return KTypeInput(this).processReflection(configBlock)
}


/**
 * See [ReflectionTypeProcessingStep]
 */
fun InputType.processReflection(configBlock: ReflectionTypeProcessingStepConfig.() -> Unit = {}): Bundle<TypeData> {
    val config = ReflectionTypeProcessingStepConfig().apply(configBlock)
    return ReflectionTypeProcessingStep(
        includeGetters = config.includeGetters,
        includeWeakGetters = config.includeWeakGetters,
        includeFunctions = config.includeFunctions,
        includeHidden = config.includeHidden,
        includeStatic = config.includeStatic,
        primitiveTypes = config.primitiveTypes,
        customProcessors = config.customProcessors,
        enumConstType = config.enumConstType,
        typeRedirects = config.typeRedirects
    ).process(this)
}


/**
 * See [ReflectionTypeProcessingStep]
 */
@JvmName("processReflectionKType")
fun Bundle<KType>.processReflection(configBlock: ReflectionTypeProcessingStepConfig.() -> Unit = {}): Bundle<TypeData> {
    return this.mapToInputType().processReflection(configBlock)
}


/**
 * See [ReflectionTypeProcessingStep]
 */
fun Bundle<InputType>.processReflection(configBlock: ReflectionTypeProcessingStepConfig.() -> Unit = {}): Bundle<TypeData> {
    val config = ReflectionTypeProcessingStepConfig().apply(configBlock)
    return ReflectionTypeProcessingStep(
        includeGetters = config.includeGetters,
        includeWeakGetters = config.includeWeakGetters,
        includeFunctions = config.includeFunctions,
        includeHidden = config.includeHidden,
        includeStatic = config.includeStatic,
        primitiveTypes = config.primitiveTypes,
        customProcessors = config.customProcessors,
        enumConstType = config.enumConstType,
        typeRedirects = config.typeRedirects
    ).process(this)
}


