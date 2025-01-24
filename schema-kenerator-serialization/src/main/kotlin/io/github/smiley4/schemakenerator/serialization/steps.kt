package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.KTypeInput
import io.github.smiley4.schemakenerator.core.data.mapToInputType
import io.github.smiley4.schemakenerator.core.steps.RenameMembersStep
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.serialization.steps.HandleJsonClassDiscriminatorStep
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationCustomProvider
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeMatcher
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeProcessingStep
import io.github.smiley4.schemakenerator.serialization.steps.fullName
import io.github.smiley4.schemakenerator.serialization.steps.matches
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


/**
 * See [HandleJsonClassDiscriminatorStep]
 */
fun Bundle<TypeData>.addJsonClassDiscriminatorProperty(): Bundle<TypeData> {
    return HandleJsonClassDiscriminatorStep().process(this)
}


/**
 * See [KotlinxSerializationTypeProcessingStep]
 */
fun KType.processKotlinxSerialization(configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}): Bundle<TypeData> {
    return KTypeInput(this).processKotlinxSerialization(configBlock)
}


/**
 * See [KotlinxSerializationTypeProcessingStep]
 */
fun SerialDescriptor.processKotlinxSerialization(configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}): Bundle<TypeData> {
    return SerialDescriptorInput(this).processKotlinxSerialization(configBlock)
}


/**
 * See [KotlinxSerializationTypeProcessingStep]
 */
fun InputType.processKotlinxSerialization(configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}): Bundle<TypeData> {
    val config = KotlinxSerializationTypeProcessingConfig().apply(configBlock)
    return KotlinxSerializationTypeProcessingStep(
        customProcessors = config.customProcessors,
        serializersModule = config.serializersModule,
        typeRedirects = config.typeRedirects,
        knownNotParameterized = config.knownNotParameterized,
    ).process(this)
}


/**
 * See [KotlinxSerializationTypeProcessingStep]
 */
@JvmName("processKotlinxSerializationKType")
fun Bundle<KType>.processKotlinxSerialization(configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}): Bundle<TypeData> {
    return this.mapToInputType().processKotlinxSerialization(configBlock)
}


/**
 * See [KotlinxSerializationTypeProcessingStep]
 */
@JvmName("processKotlinxSerializationSerialDescriptor")
fun Bundle<SerialDescriptor>.processKotlinxSerialization(
    configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}
): Bundle<TypeData> {
    return this.mapToInputType().processKotlinxSerialization(configBlock)
}


/**
 * See [KotlinxSerializationTypeProcessingStep]
 */
fun Bundle<InputType>.processKotlinxSerialization(
    configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}
): Bundle<TypeData> {
    val config = KotlinxSerializationTypeProcessingConfig().apply(configBlock)
    return KotlinxSerializationTypeProcessingStep(
        customProcessors = config.customProcessors,
        serializersModule = config.serializersModule,
        typeRedirects = config.typeRedirects,
        knownNotParameterized = config.knownNotParameterized,
    ).process(this)
}

class KotlinxSerializationTypeProcessingConfig {

    var customProcessors = mutableListOf<Pair<KotlinxSerializationTypeMatcher, KotlinxSerializationCustomProvider>>()

    var typeRedirects = mutableMapOf<String, InputType>()

    var knownNotParameterized = mutableSetOf<String>()


    /**
     * kotlinx serializers module from `Json { }.serializersModule` for support of contextual serializers
     */
    var serializersModule: SerializersModule? = null


    /**
     * Add a new custom type overwriting types matched by the given matcher.
     */
    fun custom(matcher: KotlinxSerializationTypeMatcher, provider: KotlinxSerializationCustomProvider) {
        customProcessors.add(matcher to provider)
    }


    /**
     * Add a custom type overwriting the given type.
     * Matches types by [SerialDescriptor.serialName] equals the given name.
     */
    fun custom(serializerName: String, provider: KotlinxSerializationCustomProvider) {
        custom(
            { descriptor: SerialDescriptor -> descriptor.fullName() == serializerName },
            provider
        )
    }


    /**
     * Add a custom type overwriting the given type.
     * Matches types by [SerialDescriptor.serialName] equals the qualified name of the given class.
     */
    fun custom(type: KClass<*>, provider: KotlinxSerializationCustomProvider) {
        custom(
            { descriptor: SerialDescriptor -> descriptor.matches(type) },
            provider
        )
    }


    /**
     * Add a custom type overwriting the given type.
     * Matches types by [SerialDescriptor.serialName] equals the qualified name of the given type parameter.
     */
    inline fun <reified T> custom(noinline provider: KotlinxSerializationCustomProvider) {
        custom(typeOf<T>().classifier!! as KClass<*>, provider)
    }


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

}


/**
 * See [RenameMembersStep].
 * Note: no serial descriptor or element index will be passed to the naming strategy, only the serial name
 */
@OptIn(ExperimentalSerializationApi::class)
fun Bundle<TypeData>.renameMembers(strategy: JsonNamingStrategy): Bundle<TypeData> {
    return RenameMembersStep { name ->
        strategy.serialNameForJson(PrimitiveSerialDescriptor("?", PrimitiveKind.BYTE), 0, name)
    }.process(this)
}
