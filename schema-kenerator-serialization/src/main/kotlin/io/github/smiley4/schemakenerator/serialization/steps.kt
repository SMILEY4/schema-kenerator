package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeProcessingStep
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


/**
 * See [KotlinxSerializationTypeProcessingStep]
 */
fun KType.processKotlinxSerialization(configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}): Bundle<BaseTypeData> {
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
fun Bundle<KType>.processKotlinxSerialization(configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}): Bundle<BaseTypeData> {
    val config = KotlinxSerializationTypeProcessingConfig().apply(configBlock)
    return KotlinxSerializationTypeProcessingStep(
        customProcessors = config.customProcessors,
        serializersModule = config.serializersModule,
        typeRedirects = config.typeRedirects,
        knownNotParameterized = config.knownNotParameterized,
    ).process(this)
}

class KotlinxSerializationTypeProcessingConfig {

    var customProcessors = mutableMapOf<String, () -> BaseTypeData>()

    var typeRedirects = mutableMapOf<String, KType>()

    var knownNotParameterized = mutableSetOf<String>()

    /**
     * kotlinx serializers module from `Json { }.serializersModule` for support of contextual serializers
     */
    var serializersModule: SerializersModule? = null

    /**
     * Add a custom processor for the given type that overwrites the default behaviour
     */
    fun customProcessor(serializerName: String, processor: () -> BaseTypeData) {
        customProcessors[serializerName] = processor
    }


    /**
     * Add a custom processor for the given type that overwrites the default behaviour
     */
    fun customProcessor(type: KClass<*>, processor: () -> BaseTypeData) {
        customProcessors[type.qualifiedName ?: type.java.name] = processor
    }


    /**
     * Add a custom processor for the given type that overwrites the default behaviour
     */
    inline fun <reified T> customProcessor(noinline processor: () -> BaseTypeData) {
        customProcessor(typeOf<T>().classifier!! as KClass<*>, processor)
    }


    /**
     * Add custom processors for the given types that overwrites the default behaviour
     */
    @JvmName("customProcessors_serializerName")
    fun customProcessors(processors: Map<String, () -> BaseTypeData>) {
        customProcessors.putAll(processors)
    }


    /**
     * Add custom processors for the given types that overwrites the default behaviour
     */
    @JvmName("customProcessors_type")
    fun customProcessors(processors: Map<KClass<*>, () -> BaseTypeData>) {
        processors.forEach { (k, v) ->
            customProcessors[k.qualifiedName ?: k.java.name] = v
        }
    }

    /**
     * Redirect from the given type to the other given type, i.e. when the "from" type is processed, the "to" type is used instead.
     */
    fun redirect(from: String, to: KType) {
        typeRedirects[from] = to
    }

    /**
     * Redirect from the given type to the other given type, i.e. when the "from" type is processed, the "to" type is used instead.
     */
    fun redirect(from: KType, to: KType) {
        val clazz = from.classifier!! as KClass<*>
        typeRedirects[clazz.qualifiedName ?: clazz.java.name] = to
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
    fun redirect(redirects: Map<String, KType>) {
        typeRedirects.putAll(redirects)
    }

    /**
     * Mark the type with the given full/qualified name as "not parameterized", i.e as not having any generic type parameters.
     * This helps the type processing step to determine whether two types are truly the same.
     */
    fun markNotParameterized(name: String) {
        knownNotParameterized.add(name)
    }

    /**
     * Mark the given type as "not parameterized", i.e as not having any generic type parameters.
     * This helps the type processing step to determine whether two types are truly the same.
     */
    fun  markNotParameterized(type: KType) {
        val clazz = type.classifier!! as KClass<*>
        markNotParameterized(clazz.qualifiedName ?: clazz.java.name)
    }

    /**
     * Mark the given type as "not parameterized", i.e as not having any generic type parameters.
     * This helps the type processing step to determine whether two types are truly the same.
     */
    inline fun <reified T>  markNotParameterized() {
        val clazz = typeOf<T>().classifier!! as KClass<*>
        markNotParameterized(clazz.qualifiedName ?: clazz.java.name)
    }

}
