package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.reflect.KType

interface SerializationTypeAnalyzerModule {

    class Context(
        private val analyzer: SerializationTypeAnalyzer,
        val id: TypeId,
        val descriptor: SerialDescriptor,
        val type: KType?,
        val knownTypeData: MutableList<TypeData>,
        val cache: TypeDataCache
    ) {

        fun analyze(descriptor: SerialDescriptor): WrappedTypeData {
            return this.analyzer.analyze(
                descriptor = descriptor,
                type = null,
                knownTypeData = knownTypeData,
                cache = cache,
            )
        }

    }


    /**
     * @return whether this module applies to the given type.
     */
    fun applies(descriptor: SerialDescriptor): Boolean


    /**
     * The type analysis.
     * @param context the input context with the current type to analyze and currently known additional data
     * @return [TypeData] with additional nullability information
     */
    fun analyze(context: Context): WrappedTypeData

}
