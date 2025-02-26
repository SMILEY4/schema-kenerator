package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.data.InitialKTypeData
import io.github.smiley4.schemakenerator.core.data.InitialTypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.reflection.analyzer.DefaultReflectionTypeAnalyzerModule
import io.github.smiley4.schemakenerator.reflection.analyzer.ReflectionCustomProvider
import io.github.smiley4.schemakenerator.reflection.analyzer.ReflectionTypeAnalyzerImpl
import io.github.smiley4.schemakenerator.reflection.analyzer.ReflectionTypeAnalyzerModule
import io.github.smiley4.schemakenerator.reflection.analyzer.ReflectionTypeMatcher
import io.github.smiley4.schemakenerator.reflection.analyzer.SimpleTypeAnalyzerModule
import io.github.smiley4.schemakenerator.reflection.analyzer.TypeCategoryAnalyzer.Companion.DEFAULT_PRIMITIVE_TYPES
import io.github.smiley4.schemakenerator.reflection.data.EnumConstType
import io.github.smiley4.schemakenerator.reflection.data.SubType
import io.github.smiley4.schemakenerator.reflection.data.TypeRedirect
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object ReflectionSteps {

    /**
     * Finds additional subtypes from [SubType]-annotation.
     * An additional step to add missing subtype-supertype relations later may be required.
     * Add this step before type analysis.
     * @param maxRecursionDepth how many "levels" to search for subtypes
     */
    fun InitialTypeData.collectSubTypes(maxRecursionDepth: Int = 10): InitialKTypeData {
        return when (this) {
            is InitialKTypeData -> {
                ReflectionAnnotationSubTypeStep(
                    maxRecursionDepth = maxRecursionDepth
                ).process(this)
            }
            else -> throw IllegalArgumentException("Initial type data '${this::class.simpleName}' is not supported by this step.'")
        }
    }


    /**
     * Analyze the type and using reflection and return the extracted data.
     * @param configBlock the configuration
     */
    fun InitialTypeData.analyzeTypeUsingReflection(configBlock: ReflectionTypeAnalysisConfig.() -> Unit = {}): TypeDataGroup {
        return when (this) {
            is InitialKTypeData -> {
                val config = ReflectionTypeAnalysisConfig().apply(configBlock)
                ReflectionTypeAnalyzerImpl(
                    typeRedirects = config.typeRedirects,
                    modules = config.buildCustomModules()
                ).analyze(this)
            }
            else -> throw IllegalArgumentException("Initial type data '${this::class.simpleName}' is not supported by this step.'")
        }
    }

    class ReflectionTypeAnalysisConfig {

        companion object {
            /**
             * List of default type redirects
             */
            @OptIn(ExperimentalUnsignedTypes::class)
            val DEFAULT_TYPE_REDIRECTS = mapOf(
                typeOf<BooleanArray>() to TypeRedirect(
                    fromType = typeOf<BooleanArray>(),
                    fromNullability = TypeRedirect.FromNullability.IGNORE,
                    toType = typeOf<Array<Boolean>>(),
                    toNullability = TypeRedirect.ToNullability.KEEP,
                ),
                typeOf<ByteArray>() to TypeRedirect(
                    fromType = typeOf<ByteArray>(),
                    fromNullability = TypeRedirect.FromNullability.IGNORE,
                    toType = typeOf<Array<Byte>>(),
                    toNullability = TypeRedirect.ToNullability.KEEP,
                ),
                typeOf<UByteArray>() to TypeRedirect(
                    fromType = typeOf<UByteArray>(),
                    fromNullability = TypeRedirect.FromNullability.IGNORE,
                    toType = typeOf<Array<UByte>>(),
                    toNullability = TypeRedirect.ToNullability.KEEP,
                ),
                typeOf<ShortArray>() to TypeRedirect(
                    fromType = typeOf<ShortArray>(),
                    fromNullability = TypeRedirect.FromNullability.IGNORE,
                    toType = typeOf<Array<Short>>(),
                    toNullability = TypeRedirect.ToNullability.KEEP,
                ),
                typeOf<UShortArray>() to TypeRedirect(
                    fromType = typeOf<UShortArray>(),
                    fromNullability = TypeRedirect.FromNullability.IGNORE,
                    toType = typeOf<Array<UShort>>(),
                    toNullability = TypeRedirect.ToNullability.KEEP,
                ),
                typeOf<CharArray>() to TypeRedirect(
                    fromType = typeOf<CharArray>(),
                    fromNullability = TypeRedirect.FromNullability.IGNORE,
                    toType = typeOf<Array<Char>>(),
                    toNullability = TypeRedirect.ToNullability.KEEP,
                ),
                typeOf<IntArray>() to TypeRedirect(
                    fromType = typeOf<IntArray>(),
                    fromNullability = TypeRedirect.FromNullability.IGNORE,
                    toType = typeOf<Array<Int>>(),
                    toNullability = TypeRedirect.ToNullability.KEEP,
                ),
                typeOf<UIntArray>() to TypeRedirect(
                    fromType = typeOf<UIntArray>(),
                    fromNullability = TypeRedirect.FromNullability.IGNORE,
                    toType = typeOf<Array<UInt>>(),
                    toNullability = TypeRedirect.ToNullability.KEEP,
                ),
                typeOf<LongArray>() to TypeRedirect(
                    fromType = typeOf<LongArray>(),
                    fromNullability = TypeRedirect.FromNullability.IGNORE,
                    toType = typeOf<Array<Long>>(),
                    toNullability = TypeRedirect.ToNullability.KEEP,
                ),
                typeOf<ULongArray>() to TypeRedirect(
                    fromType = typeOf<ULongArray>(),
                    fromNullability = TypeRedirect.FromNullability.IGNORE,
                    toType = typeOf<Array<ULong>>(),
                    toNullability = TypeRedirect.ToNullability.KEEP,
                ),
                typeOf<FloatArray>() to TypeRedirect(
                    fromType = typeOf<FloatArray>(),
                    fromNullability = TypeRedirect.FromNullability.IGNORE,
                    toType = typeOf<Array<Float>>(),
                    toNullability = TypeRedirect.ToNullability.KEEP,
                ),
                typeOf<DoubleArray>() to TypeRedirect(
                    fromType = typeOf<DoubleArray>(),
                    fromNullability = TypeRedirect.FromNullability.IGNORE,
                    toType = typeOf<Array<Double>>(),
                    toNullability = TypeRedirect.ToNullability.KEEP,
                ),
            )
        }


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
        var typeRedirects = mutableListOf<TypeRedirect>().also {
            DEFAULT_TYPE_REDIRECTS.forEach { (_, value) -> it.add(value) }
        }


        /**
         * Redirect from a given type to another given type, i.e. if the specified type is encountered, replace it with the provided type.
         */
        fun redirect(config: RedirectConfig.() -> Unit) {
            typeRedirects.add(RedirectConfig().apply(config).build())
        }

        class RedirectConfig {

            private var fromType: KType? = null
            private var fromNullability: TypeRedirect.FromNullability = TypeRedirect.FromNullability.IGNORE
            private var toType: KType? = null
            private var toNullability: TypeRedirect.ToNullability = TypeRedirect.ToNullability.KEEP


            /**
             * Specify the original type to replace.
             * @param nullability specify the behavior how to handle the nullability of the original type
             * @param T the type to replace
             */
            inline fun <reified T> from(nullability: TypeRedirect.FromNullability = TypeRedirect.FromNullability.IGNORE) {
                from(typeOf<T>(), nullability)
            }


            /**
             * Specify the original type to replace.
             * @param type the type to replace
             * @param nullability specify the behavior how to handle the nullability of the original type
             */
            fun from(type: KType, nullability: TypeRedirect.FromNullability = TypeRedirect.FromNullability.IGNORE) {
                fromType = type
                fromNullability = nullability
            }


            /**
             * Specify the target type.
             * @param nullability specify the behavior how to handle the nullability of the target type
             * @param T the type to replace with
             */
            inline fun <reified T> to(nullability: TypeRedirect.ToNullability = TypeRedirect.ToNullability.KEEP) {
                to(typeOf<T>(), nullability)
            }


            /**
             * Specify the target type.
             * @param type the type to replace with
             * @param nullability specify the behavior how to handle the nullability of the target type
             */
            fun to(type: KType, nullability: TypeRedirect.ToNullability = TypeRedirect.ToNullability.KEEP) {
                toType = type
                toNullability = nullability
            }


            /**
             * Creates the final [TypeRedirect] from this config.
             */
            internal fun build(): TypeRedirect {
                return TypeRedirect(
                    fromType = fromType ?: throw IllegalArgumentException("Redirect configuration is missing 'from' type."),
                    toType = toType ?: throw IllegalArgumentException("Redirect configuration is missing 'from' type."),
                    fromNullability = fromNullability,
                    toNullability = toNullability
                )
            }

        }

    }

}
