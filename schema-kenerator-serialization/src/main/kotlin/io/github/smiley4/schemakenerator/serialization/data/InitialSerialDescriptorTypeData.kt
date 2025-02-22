package io.github.smiley4.schemakenerator.serialization.data

import io.github.smiley4.schemakenerator.core.data.InitialTypeData
import kotlinx.serialization.descriptors.SerialDescriptor

/**
 * [InitialTypeData] for [SerialDescriptor]s.
 */
class InitialSerialDescriptorTypeData(
    /**
     * the root type / serial descriptor.
     */
    val type: SerialDescriptor,
) : InitialTypeData