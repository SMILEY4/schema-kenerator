package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.mapToInputType
import io.github.smiley4.schemakenerator.core.renameMembers
import io.github.smiley4.schemakenerator.serialization.analyzer.DefaultSerializationTypeAnalyzerModule
import io.github.smiley4.schemakenerator.serialization.analyzer.KotlinxSerializationCustomProvider
import io.github.smiley4.schemakenerator.serialization.analyzer.KotlinxSerializationTypeMatcher
import io.github.smiley4.schemakenerator.serialization.analyzer.SerializationTypeAnalyzerImpl
import io.github.smiley4.schemakenerator.serialization.analyzer.SerializationTypeAnalyzerModule
import io.github.smiley4.schemakenerator.serialization.analyzer.SimpleSerializationTypeAnalyzerModule
import io.github.smiley4.schemakenerator.serialization.analyzer.fullName
import io.github.smiley4.schemakenerator.serialization.data.SerialDescriptorInput
import io.github.smiley4.schemakenerator.serialization.data.mapToInputType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


/**
 * Handles the [JsonClassDiscriminator]-annotations and adds a discriminator property with the defined name and
 * annotated with a marker annotation called [io.github.smiley4.schemakenerator.core.AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME]
 */
fun Bundle<TypeData>.addJsonClassDiscriminatorProperty(): Bundle<TypeData> {
    return HandleJsonClassDiscriminatorStep().process(this)
}


/**
 * Renames members of types according to the given [JsonNamingStrategy].
 * Add this step after type analysis and before schema generation.
 */
@OptIn(ExperimentalSerializationApi::class)
fun Bundle<TypeData>.renameMembers(strategy: JsonNamingStrategy): Bundle<TypeData> {
    return this.renameMembers { name ->
        strategy.serialNameForJson(PrimitiveSerialDescriptor("?", PrimitiveKind.BYTE), 0, name)
    }
}


/**
 * Analyze the type and using kotlinx-serialization and return the extracted data.
 * @param configBlock the configuration
 */
fun KType.analyzeTypeUsingKotlinxSerialization(
    configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}
): Bundle<TypeData> {
    return KTypeInput(this).analyzeTypeUsingKotlinxSerialization(configBlock)
}


/**
 * Analyze the type and using kotlinx-serialization and return the extracted data.
 * @param configBlock the configuration
 */
fun SerialDescriptor.analyzeTypeUsingKotlinxSerialization(
    configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}
): Bundle<TypeData> {
    return SerialDescriptorInput(this).analyzeTypeUsingKotlinxSerialization(configBlock)
}


/**
 * Analyze the type and using kotlinx-serialization and return the extracted data.
 * @param configBlock the configuration
 */
fun InputType.analyzeTypeUsingKotlinxSerialization(
    configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}
): Bundle<TypeData> {
    val config = KotlinxSerializationTypeProcessingConfig().apply(configBlock)
    return SerializationTypeAnalyzerImpl(
        serializersModule = config.serializersModule,
        typeRedirects = config.typeRedirects,
        modules = config.buildCustomModules()
    ).analyze(this)
}


/**
 * Analyze the type and using kotlinx-serialization and return the extracted data.
 * @param configBlock the configuration
 */
@JvmName("processKotlinxSerializationKType")
fun Bundle<KType>.analyzeTypeUsingKotlinxSerialization(
    configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}
): Bundle<TypeData> {
    return this.mapToInputType().analyzeTypeUsingKotlinxSerialization(configBlock)
}


/**
 * Analyze the type and using kotlinx-serialization and return the extracted data.
 * @param configBlock the configuration
 */
@JvmName("processKotlinxSerializationSerialDescriptor")
fun Bundle<SerialDescriptor>.analyzeTypeUsingKotlinxSerialization(
    configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}
): Bundle<TypeData> {
    return this.mapToInputType().analyzeTypeUsingKotlinxSerialization(configBlock)
}


/**
 * Analyze the type and using kotlinx-serialization and return the extracted data.
 * @param configBlock the configuration
 */
fun Bundle<InputType>.analyzeTypeUsingKotlinxSerialization(
    configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}
): Bundle<TypeData> {
    val config = KotlinxSerializationTypeProcessingConfig().apply(configBlock)
    return SerializationTypeAnalyzerImpl(
        serializersModule = config.serializersModule,
        typeRedirects = config.typeRedirects,
        modules = config.buildCustomModules()
    ).analyze(this)
}

class KotlinxSerializationTypeProcessingConfig {

    /**
     * kotlinx serializers module from `Json { }.serializersModule` for support of contextual serializers
     */
    var serializersModule: SerializersModule? = null

    var knownNotParameterized = mutableSetOf<String>()


    /**
     * Mark the type with the given full/qualified name as "not parameterized", i.e. as not having any generic type parameters.
     * This helps the type processing step to determine whether two types are truly the same.
     */
    fun markNotParameterized(name: String) {
        knownNotParameterized.add(name)
    }


    /**
     * Mark the given type as "not parameterized", i.e as not having any generic type parameters.
     * This helps the type processing step to determine whether two types are truly the same.
     */
    fun markNotParameterized(type: KType) {
        val clazz = type.classifier!! as KClass<*>
        markNotParameterized(clazz.qualifiedName ?: clazz.java.name)
    }


    /**
     * Mark the given type as "not parameterized", i.e as not having any generic type parameters.
     * This helps the type processing step to determine whether two types are truly the same.
     */
    inline fun <reified T> markNotParameterized() {
        val clazz = typeOf<T>().classifier!! as KClass<*>
        markNotParameterized(clazz.qualifiedName ?: clazz.java.name)
    }


    var customModules = mutableListOf<SerializationTypeAnalyzerModule>()

    internal fun buildCustomModules(): List<SerializationTypeAnalyzerModule> {
        val allModules = listOf(
            DefaultSerializationTypeAnalyzerModule(
                knownNotParameterized = knownNotParameterized
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


    var typeRedirects = mutableMapOf<String, InputType>()


    /**
     * Redirect from the given type to the other given type, i.e. when the "from" type is processed, the "to" type is used instead.
     */
    fun redirect(from: String, to: KType) {
        typeRedirects[from] = KTypeInput(to)
    }


    /**
     * Redirect from the given type to the other given type, i.e. when the "from" type is processed, the "to" type is used instead.
     */
    fun redirect(from: KType, to: KType) {
        val clazz = from.classifier!! as KClass<*>
        val idFrom = (clazz.qualifiedName ?: clazz.java.name).let {
            it + if (from.isMarkedNullable) "?" else ""
        }
        typeRedirects[idFrom] = KTypeInput(to)
    }


    /**
     * Redirect from the given type to the other given type, i.e. when the "from" type is processed, the "to" type is used instead.
     */
    inline fun <reified FROM, reified TO> redirect() {
        redirect(typeOf<FROM>(), typeOf<TO>())
    }

}
