package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
import kotlinx.serialization.descriptors.SerialDescriptor

/**
 * Analyses the given type and returns the resulting [TypeData] (with additional nullability information)
 */
interface SerializationTypeAnalyzer {

    fun analyze(
        descriptor: SerialDescriptor,
        nullable: Boolean,
        knownTypeData: MutableList<TypeData>,
        processedDescriptors: MutableMap<SerialDescriptor, TypeData>
    ): WrappedTypeData

}
