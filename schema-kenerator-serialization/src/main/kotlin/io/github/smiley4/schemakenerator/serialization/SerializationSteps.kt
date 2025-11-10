package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.AddEnumDiscriminatorStep
import io.github.smiley4.schemakenerator.core.AddStringDiscriminatorStep
import io.github.smiley4.schemakenerator.core.CoreSteps.renameMembers
import io.github.smiley4.schemakenerator.core.data.InitialTypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.merge
import io.github.smiley4.schemakenerator.jsonschema.data.CompiledJsonSchemaData
import io.github.smiley4.schemakenerator.serialization.analyzer.DefaultSerializationTypeAnalyzerModule
import io.github.smiley4.schemakenerator.serialization.analyzer.KotlinxSerializationCustomProvider
import io.github.smiley4.schemakenerator.serialization.analyzer.KotlinxSerializationTypeMatcher
import io.github.smiley4.schemakenerator.serialization.analyzer.SerializationTypeAnalyzerImpl
import io.github.smiley4.schemakenerator.serialization.analyzer.SerializationTypeAnalyzerModule
import io.github.smiley4.schemakenerator.serialization.analyzer.SimpleSerializationTypeAnalyzerModule
import io.github.smiley4.schemakenerator.serialization.analyzer.fullName
import io.github.smiley4.schemakenerator.serialization.data.InitialSerialDescriptorTypeData
import io.github.smiley4.schemakenerator.serialization.data.TypeRedirect
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object SerializationSteps {

    /**
     * Create a new [InitialSerialDescriptorTypeData] for the given type.
     * @param type the initial (root) type
     */
    fun initial(type: SerialDescriptor) = InitialSerialDescriptorTypeData(
        type = type,
    )


    /**
     * Handles the [JsonClassDiscriminator]-annotations and adds a discriminator property with the defined name and
     * annotated with a marker annotation called [AddStringDiscriminatorStep.MARKER_ANNOTATION_NAME]
     * @param asEnum whether the property should be a simple string or an enum with the (full identifying) name of the type as only option.
     */
    fun TypeDataGroup.addJsonClassDiscriminatorProperty(asEnum: Boolean = false): TypeDataGroup {
        return when (asEnum) {
            true -> AddEnumDiscriminatorStep(KotlinxJsonDiscriminatorNameProvider()).process(this)
            false -> AddStringDiscriminatorStep(KotlinxJsonDiscriminatorNameProvider()).process(this)
        }
    }


    /**
     * Renames members of types according to the given [JsonNamingStrategy].
     * Add this step after type analysis and before schema generation.
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun TypeDataGroup.renameMembers(strategy: JsonNamingStrategy): TypeDataGroup {
        return this.renameMembers { name ->
            strategy.serialNameForJson(PrimitiveSerialDescriptor("?", PrimitiveKind.BYTE), 0, name)
        }
    }


    /**
     * Analyze the type and using kotlinx-serialization and return the extracted data.
     * @param configBlock the configuration
     */
    fun InitialTypeData.analyzeTypeUsingKotlinxSerialization(
        configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}
    ): TypeDataGroup {
        val config = KotlinxSerializationTypeProcessingConfig().apply(configBlock)
        return SerializationTypeAnalyzerImpl(
            serializersModule = config.serializersModule,
            typeRedirects = config.typeRedirects,
            modules = config.buildCustomModules(),
        ).analyze(this)
    }

    class KotlinxSerializationTypeProcessingConfig {

        /**
         * Attempt to determine type parameters using reflection. Can result in improved titles in schemas.
         */
        var findTypeParametersUsingReflection: Boolean = true


        /**
         * kotlinx serializers module from `Json { }.serializersModule` for support of contextual serializers
         */
        var serializersModule: SerializersModule? = null


        @Deprecated("unused")
        var knownNotParameterized = mutableSetOf<String>()


        /**
         * Mark the type with the given full/qualified name as "not parameterized", i.e. as not having any generic type parameters.
         * This helps the type processing step to determine whether two types are truly the same.
         */
        @Deprecated("unused")
        fun markNotParameterized(name: String) {
            knownNotParameterized.add(name)
        }


        /**
         * Mark the given type as "not parameterized", i.e. as not having any generic type parameters.
         * This helps the type processing step to determine whether two types are truly the same.
         */
        @Deprecated("unused")
        fun markNotParameterized(type: KType) {
            val clazz = type.classifier!! as KClass<*>
            markNotParameterized(clazz.qualifiedName ?: clazz.java.name)
        }


        /**
         * Mark the given type as "not parameterized", i.e as not having any generic type parameters.
         * This helps the type processing step to determine whether two types are truly the same.
         */
        @Deprecated("unused")
        inline fun <reified T> markNotParameterized() {
            val clazz = typeOf<T>().classifier!! as KClass<*>
            markNotParameterized(clazz.qualifiedName ?: clazz.java.name)
        }


        var customModules = mutableListOf<SerializationTypeAnalyzerModule>()

        internal fun buildCustomModules(): List<SerializationTypeAnalyzerModule> {
            val allModules = listOf(
                DefaultSerializationTypeAnalyzerModule(
                    findTypeParametersUsingReflection = findTypeParametersUsingReflection
                )
            ) + customModules
            return allModules.reversed()
        }


        /**
         * Adds a new [SerializationTypeAnalyzerModule].
         * Modules overwrite previous modules when matching the same type.
         */
        fun custom(module: SerializationTypeAnalyzerModule) {
            customModules.add(module)
        }


        /**
         * Add a new custom type for types matched by the given matcher.
         * Modules overwrite previous modules when matching the same type.
         */
        fun custom(matcher: KotlinxSerializationTypeMatcher, provider: KotlinxSerializationCustomProvider) {
            custom(SimpleSerializationTypeAnalyzerModule(matcher, provider))
        }


        /**
         * Add a new custom type for types matched by the given serial name.
         * Modules overwrite previous modules when matching the same type.
         */
        fun custom(serializerName: String, provider: KotlinxSerializationCustomProvider) {
            custom(
                { descriptor: SerialDescriptor -> descriptor.fullName() == serializerName },
                provider
            )
        }


        /**
         * Add a custom type overwriting the given type.
         * Modules overwrite previous modules when matching the same type.
         */
        fun custom(type: KClass<*>, provider: KotlinxSerializationCustomProvider) {
            custom(
                { descriptor: SerialDescriptor -> descriptor.fullName() == (type.qualifiedName ?: type.java.name) },
                provider
            )
        }


        /**
         * Add a custom type overwriting the given type.
         * Modules overwrite previous modules when matching the same type.
         */
        inline fun <reified T> custom(noinline provider: KotlinxSerializationCustomProvider) {
            custom(typeOf<T>().classifier!! as KClass<*>, provider)
        }


        /**
         * list of configured type redirects.
         */
        var typeRedirects = mutableListOf<TypeRedirect>()


        /**
         * Redirect from a given type to another given type, i.e. if the specified type is encountered, replace it with the provided type.
         */
        fun redirect(config: RedirectConfig.() -> Unit) {
            typeRedirects.add(RedirectConfig().apply(config).build())
        }

        class RedirectConfig {

            private var fromType: String? = null
            private var fromTypeNullable: Boolean = false
            private var fromNullability: TypeRedirect.FromNullability = TypeRedirect.FromNullability.IGNORE
            private var toType: Pair<SerialDescriptor?, KType?>? = null
            private var toNullability: TypeRedirect.ToNullability = TypeRedirect.ToNullability.KEEP


            /**
             * Specify the original type to replace.
             * @param T the type to replace
             * @param nullability specify the behavior how to handle the nullability of the original type
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
                val clazz = type.classifier!! as KClass<*>
                val name = clazz.qualifiedName ?: clazz.java.name
                from(name, type.isMarkedNullable, nullability)
            }


            /**
             * Specify the original type to replace.
             * @param type the qualified name of the type or serial descriptor to replace
             * @param nullable whether the [type] is nullable
             * @param nullability specify the behavior how to handle the nullability of the original type
             */
            fun from(
                type: String,
                nullable: Boolean = false,
                nullability: TypeRedirect.FromNullability = TypeRedirect.FromNullability.IGNORE
            ) {
                fromType = type
                fromTypeNullable = nullable
                fromNullability = nullability
            }


            /**
             * Specify the target type.
             * @param T the type to replace with
             * @param nullability specify the behavior how to handle the nullability of the target type
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
                toType = null to type
                toNullability = nullability
            }


            /**
             * Specify the target type.
             * @param type the type to replace with
             * @param nullability specify the behavior how to handle the nullability of the target type
             */
            fun to(type: SerialDescriptor, nullability: TypeRedirect.ToNullability = TypeRedirect.ToNullability.KEEP) {
                toType = type to null
                toNullability = nullability
            }


            /**
             * Creates the final [TypeRedirect] from this config.
             */
            internal fun build(): TypeRedirect {
                return TypeRedirect(
                    fromType = fromType ?: throw IllegalArgumentException("Redirect configuration is missing 'from' type."),
                    fromTypeNullable = fromTypeNullable,
                    toType = toType ?: throw IllegalArgumentException("Redirect configuration is missing 'from' type."),
                    fromNullability = fromNullability,
                    toNullability = toNullability
                )
            }

        }

    }

    fun CompiledJsonSchemaData.convertToKotlinxSerializationTypes(): JsonElement {
        return this
            .merge()
            .let { KotlinxJsonConvertJsonTypes().process(it) }
    }

}
