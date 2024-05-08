package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeProcessingStep
import kotlin.reflect.KClass
import kotlin.reflect.KType


class KotlinxSerializationTypeProcessingStepConfig {
    internal var customProcessors = mutableMapOf<String, () -> BaseTypeData>()

    fun customProcessor(serializerName: String, processor: () -> BaseTypeData) {
        customProcessors[serializerName] = processor
    }

    fun customProcessor(type: KClass<*>, processor: () -> BaseTypeData) {
        customProcessors[type.qualifiedName!!] = processor
    }


    @JvmName("customProcessors_serializerName")
    fun customProcessors(processors: Map<String, () -> BaseTypeData>) {
        customProcessors.putAll(processors)
    }


    @JvmName("customProcessors_type")
    fun customProcessors(processors: Map<KClass<*>, () -> BaseTypeData>) {
        processors.forEach { (k, v) ->
            customProcessors[k.qualifiedName!!] = v
        }
    }

}


/**
 * See [KotlinxSerializationTypeProcessingStep]
 */
fun KType.processKotlinxSerialization(configBlock: KotlinxSerializationTypeProcessingStepConfig.() -> Unit = {}): Bundle<BaseTypeData> {
    val config = KotlinxSerializationTypeProcessingStepConfig().apply(configBlock)
    return KotlinxSerializationTypeProcessingStep(
        customProcessors = config.customProcessors
    ).process(this)
}


/**
 * See [KotlinxSerializationTypeProcessingStep]
 */
fun Bundle<KType>.processKotlinxSerialization(configBlock: KotlinxSerializationTypeProcessingStepConfig.() -> Unit = {}): Bundle<BaseTypeData> {
    val config = KotlinxSerializationTypeProcessingStepConfig().apply(configBlock)
    return KotlinxSerializationTypeProcessingStep(
        customProcessors = config.customProcessors
    ).process(this)
}


