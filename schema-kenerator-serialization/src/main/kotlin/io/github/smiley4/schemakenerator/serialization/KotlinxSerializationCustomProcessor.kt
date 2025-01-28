package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.data.TypeData
import kotlinx.serialization.descriptors.SerialDescriptor

typealias KotlinxSerializationTypeMatcher = (descriptor: SerialDescriptor) -> Boolean

typealias KotlinxSerializationCustomProvider = () -> TypeData
