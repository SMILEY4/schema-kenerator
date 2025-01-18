package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.data.InputType
import io.github.smiley4.schemakenerator.core.data.map
import kotlinx.serialization.descriptors.SerialDescriptor

class SerialDescriptorInput(val descriptor: SerialDescriptor) : InputType

fun Bundle<SerialDescriptor>.mapToInputType(): Bundle<InputType> {
    return this.map { SerialDescriptorInput(it) }
}
