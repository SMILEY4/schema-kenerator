package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import kotlinx.serialization.descriptors.SerialDescriptor

/**
 * Matches a given descriptor to apply a custom provider to
 */
typealias KotlinxSerializationTypeMatcher = (descriptor: SerialDescriptor) -> Boolean

/**
 * Provide type data for a matched descriptor
 */
typealias KotlinxSerializationCustomProvider = (id: TypeId) -> TypeData
