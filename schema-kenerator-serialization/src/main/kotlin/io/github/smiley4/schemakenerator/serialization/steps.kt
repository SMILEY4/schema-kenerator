package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.data.BaseTypeData
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeProcessingStep
import kotlin.reflect.KType


/**
 * See [KotlinxSerializationTypeProcessingStep]
 */
fun Collection<KType>.processKotlinxSerialization(): Collection<BaseTypeData> {
    return KotlinxSerializationTypeProcessingStep().process(this)
}


