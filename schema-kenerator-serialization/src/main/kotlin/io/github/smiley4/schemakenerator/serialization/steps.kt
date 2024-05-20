package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeProcessingStep
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


/**
 * See [KotlinxSerializationTypeProcessingStep]
 */
fun KType.processKotlinxSerialization(configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}): Bundle<BaseTypeData> {
    val config = KotlinxSerializationTypeProcessingConfig().apply(configBlock)
    return KotlinxSerializationTypeProcessingStep(
        customProcessors = config.customProcessors
    ).process(this)
}


/**
 * See [KotlinxSerializationTypeProcessingStep]
 */
fun Bundle<KType>.processKotlinxSerialization(configBlock: KotlinxSerializationTypeProcessingConfig.() -> Unit = {}): Bundle<BaseTypeData> {
    val config = KotlinxSerializationTypeProcessingConfig().apply(configBlock)
    return KotlinxSerializationTypeProcessingStep(
        customProcessors = config.customProcessors
    ).process(this)
}

class KotlinxSerializationTypeProcessingConfig {
    internal var customProcessors = mutableMapOf<String, () -> BaseTypeData>()

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
        customProcessors[type.qualifiedName!!] = processor
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
            customProcessors[k.qualifiedName!!] = v
        }
    }

}
