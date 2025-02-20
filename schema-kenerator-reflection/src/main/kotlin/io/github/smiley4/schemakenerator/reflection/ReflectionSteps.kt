package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.mapToInputType
import io.github.smiley4.schemakenerator.reflection.analyzer.DefaultReflectionTypeAnalyzerModule
import io.github.smiley4.schemakenerator.reflection.analyzer.ReflectionCustomProvider
import io.github.smiley4.schemakenerator.reflection.analyzer.ReflectionTypeAnalyzerImpl
import io.github.smiley4.schemakenerator.reflection.analyzer.ReflectionTypeAnalyzerModule
import io.github.smiley4.schemakenerator.reflection.analyzer.ReflectionTypeMatcher
import io.github.smiley4.schemakenerator.reflection.analyzer.SimpleTypeAnalyzerModule
import io.github.smiley4.schemakenerator.reflection.analyzer.TypeCategoryAnalyzer.Companion.DEFAULT_PRIMITIVE_TYPES
import io.github.smiley4.schemakenerator.reflection.data.EnumConstType
import io.github.smiley4.schemakenerator.reflection.data.SubType
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object ReflectionSteps {

    /**
     * Finds additional subtypes from [SubType]-annotation.
     * An additional step to add missing subtype-supertype relations .later may be required
     * Add this step before type analysis.
     * @param maxRecursionDepth how many "levels" to search for subtypes
     */
    fun KType.collectSubTypes(maxRecursionDepth: Int = 10): Bundle<InputType> {
        return KTypeInput(this).collectSubTypes(maxRecursionDepth)
    }


    /**
     * Finds additional subtypes from [SubType]-annotation.
     * An additional step to add missing subtype-supertype relations later may be required.
     * Add this step before type analysis.
     * @param maxRecursionDepth how many "levels" to search for subtypes
     */
    fun InputType.collectSubTypes(maxRecursionDepth: Int = 10): Bundle<InputType> {
        return ReflectionAnnotationSubTypeStep(
            maxRecursionDepth = maxRecursionDepth
        ).process(this)
    }


    /**
     * Analyze the type and using reflection and return the extracted data.
     * @param configBlock the configuration
     */
    fun KType.analyzeTypeUsingReflection(configBlock: ReflectionTypeAnalysisConfig.() -> Unit = {}): Bundle<TypeData> {
        return KTypeInput(this).analyzeTypeUsingReflection(configBlock)
    }


    /**
     * Analyze the type and using reflection and return the extracted data.
     * @param configBlock the configuration
     */
    fun InputType.analyzeTypeUsingReflection(configBlock: ReflectionTypeAnalysisConfig.() -> Unit = {}): Bundle<TypeData> {
        val config = ReflectionTypeAnalysisConfig().apply(configBlock)
        return ReflectionTypeAnalyzerImpl(
            typeRedirects = config.typeRedirects,
            modules = config.buildCustomModules()
        ).analyze(this)
    }


    /**
     * Analyze the type and using reflection and return the extracted data.
     * @param configBlock the configuration
     */
    @JvmName("analyseKTypeUsingReflection")
    fun Bundle<KType>.analyzeTypeUsingReflection(configBlock: ReflectionTypeAnalysisConfig.() -> Unit = {}): Bundle<TypeData> {
        return this.mapToInputType().analyzeTypeUsingReflection(configBlock)
    }


    /**
     * Analyze the type and using reflection and return the extracted data.
     * @param configBlock the configuration
     */
    fun Bundle<InputType>.analyzeTypeUsingReflection(configBlock: ReflectionTypeAnalysisConfig.() -> Unit = {}): Bundle<TypeData> {
        val config = ReflectionTypeAnalysisConfig().apply(configBlock)
        return ReflectionTypeAnalyzerImpl(
            typeRedirects = config.typeRedirects,
            modules = config.buildCustomModules()
        ).analyze(this)
    }


    class ReflectionTypeAnalysisConfig {

        /**
         * Whether to include getters as members of classes (see [io.github.smiley4.schemakenerator.core.data.MemberKind.GETTER]).
         */
        var includeGetters: Boolean = false


        /**
         * Whether to include weak getters as members of classes (see [io.github.smiley4.schemakenerator.core.data.MemberKind.WEAK_GETTER]).
         */
        var includeWeakGetters: Boolean = false


        /**
         * Whether to include functions as members of classes (see [io.github.smiley4.schemakenerator.core.data.MemberKind.FUNCTION]).
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
         * The list of types that are considered "primitive types"
         */
        var primitiveTypes: MutableSet<KClass<*>> = DEFAULT_PRIMITIVE_TYPES.toMutableSet()


        /**
         * Whether to use "toString" for enum values or the declared "name"
         */
        var enumConstType: EnumConstType = EnumConstType.NAME


        /**
         * List of configured [ReflectionTypeAnalyzerModule] to use for analysis.
         */
        var modules = mutableListOf<ReflectionTypeAnalyzerModule>()

        internal fun buildCustomModules(): List<ReflectionTypeAnalyzerModule> {
            val allModules = listOf(
                DefaultReflectionTypeAnalyzerModule(
                    includeGetters = includeGetters,
                    includeWeakGetters = includeWeakGetters,
                    includeFunctions = includeFunctions,
                    includeHidden = includeHidden,
                    includeStatic = includeStatic,
                    primitiveTypes = primitiveTypes,
                    enumConstType = enumConstType,
                )
            ) + modules
            return allModules.reversed()
        }


        /**
         * Adds a new [ReflectionTypeAnalyzerModule].
         * Modules overwrite previous modules when matching the same type.
         */
        fun custom(module: ReflectionTypeAnalyzerModule) {
            modules.add(module)
        }


        /**
         * Add a new custom type for types matched by the given matcher.
         * Modules overwrite previous modules when matching the same type.
         */
        fun custom(matcher: ReflectionTypeMatcher, provider: ReflectionCustomProvider) {
            modules.add(SimpleTypeAnalyzerModule(matcher, provider))
        }


        /**
         * Add a custom type overwriting the given type.
         * Modules overwrite previous modules when matching the same type.
         */
        fun custom(clazz: KClass<*>, provider: ReflectionCustomProvider) {
            custom(
                { _: KType, c: KClass<*> -> c == clazz },
                provider
            )
        }


        /**
         * Add a custom type overwriting the given type.
         * Modules overwrite previous modules when matching the same type.
         */
        inline fun <reified T> custom(noinline provider: ReflectionCustomProvider) {
            custom(typeOf<T>().classifier!! as KClass<*>, provider)
        }


        /**
         * list of configured type redirects.
         */
        var typeRedirects = mutableMapOf<KType, KType>().also { it.putAll(ReflectionTypeAnalyzerImpl.DEFAULT_REDIRECTS) }


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

}
