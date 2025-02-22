package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nonNullOriginal

@OptIn(ExperimentalSerializationApi::class)
class TypeDataCache(private val entries: MutableMap<SerialDescriptor, TypeData> = mutableMapOf()) {

    /**
     * Cache the given [TypeData] for the given [SerialDescriptor]. The nullability of the descriptor is irrelevant
     * @param descriptor the descriptor resulting in the given type data
     * @param typeData the [TypeData] for the given descriptor
     * */
    operator fun set(descriptor: SerialDescriptor, typeData: TypeData) {
        entries[descriptor.nonNullOriginal] = typeData
    }


    /**
     * Retrieve the [TypeData] for the given [SerialDescriptor], regardless of nullability of the descriptor
     * @param descriptor the [SerialDescriptor] for the type data
     * @return the [TypeData] for the given serial descriptor or null
     */
    operator fun get(descriptor: SerialDescriptor): TypeData? {
        return entries[descriptor.nonNullOriginal]
    }

}