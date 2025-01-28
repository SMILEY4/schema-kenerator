package io.github.smiley4.schemakenerator.serialization.steps

import io.github.smiley4.schemakenerator.core.typedata.TypeData
import kotlinx.serialization.descriptors.SerialDescriptor

typealias KotlinxSerializationTypeMatcher = (descriptor: SerialDescriptor) -> Boolean

typealias KotlinxSerializationCustomProvider = () -> TypeData
