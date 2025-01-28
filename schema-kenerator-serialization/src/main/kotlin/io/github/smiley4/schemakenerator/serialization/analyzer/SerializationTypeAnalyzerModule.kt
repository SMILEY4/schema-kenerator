package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.core.typedata.TypeId
import io.github.smiley4.schemakenerator.core.typedata.WrappedTypeData
import kotlinx.serialization.descriptors.SerialDescriptor

interface SerializationTypeAnalyzerModule {

    class Context(
        private val analyzer: SerializationTypeAnalyzer,
        val id: TypeId,
        val descriptor: SerialDescriptor,
        val nullable: Boolean,
        val knownTypeData: MutableList<TypeData>,
        val processedDescriptors: MutableMap<SerialDescriptor, TypeData>
    ) {

        fun analyze(descriptor: SerialDescriptor): WrappedTypeData {
            return this.analyzer.analyze(
                descriptor = descriptor,
                nullable = false,
                knownTypeData = knownTypeData,
                processedDescriptors = processedDescriptors,
            )
        }

    }


    /**
     * @return whether this module applies to the given type.
     */
    fun applies(descriptor: SerialDescriptor): Boolean


    /**
     * The type analysis.
     * @param context the input context with the current type to analyse and currently known additional data
     * @return [TypeData] with additional nullability information
     */
    fun analyze(context: Context): WrappedTypeData

}