package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
import kotlinx.serialization.descriptors.SerialDescriptor

/**
 * Analyzes the given type and returns the resulting [TypeData] (with additional nullability information)
 */
interface SerializationTypeAnalyzer {

    /**
     * Analyzes the given descriptor and adds the results to the given collection
     * @param descriptor the input descriptor to parse
     * @param knownTypeParameters possible type parameters determined in advance. These may or may not be accurate.
     * @param knownTypeData the already known type data. Adds new results to this collection.
     * @param cache already processed descriptors with their type data. Adds new results to this map.
     */
    fun analyze(
        descriptor: SerialDescriptor,
        knownTypeParameters: List<SerialDescriptor>,
        knownTypeData: MutableList<TypeData>,
        cache: TypeDataCache
    ): WrappedTypeData

}
