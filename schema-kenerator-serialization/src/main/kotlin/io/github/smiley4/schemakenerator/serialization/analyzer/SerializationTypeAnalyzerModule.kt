package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeDataUtils.matches
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.data.TypeName
import io.github.smiley4.schemakenerator.core.data.TypeParameterData
import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
import kotlinx.serialization.descriptors.SerialDescriptor

interface SerializationTypeAnalyzerModule {

    class Context(
        private val analyzer: SerializationTypeAnalyzer,
        val id: TypeId,
        val descriptor: SerialDescriptor,
        val knownTypeData: MutableList<TypeData>,
        val cache: TypeDataCache
    ) {

        fun analyze(descriptor: SerialDescriptor): WrappedTypeData {
            return this.analyzer.analyze(
                descriptor = descriptor,
                knownTypeData = knownTypeData,
                cache = cache,
            )
        }

        fun findKnown(
            identifyingName: TypeName = descriptor.toTypeName(),
            descriptiveName: TypeName = descriptor.toTypeName(),
            typeParameters: List<TypeParameterData> = emptyList()) : TypeData?
        {
            // Only find a matching type by name AND serial descriptor.  The addition of comparing serial descriptors is
            // REQUIRED when the types have matching @SerialName values.
            return knownTypeData.find { known ->
                known.matches(identifyingName, descriptiveName, typeParameters) &&
                        cache.getDescriptor(known)?.equals(descriptor) ?: false
            }
        }

        /**
         * @return a [TypeName] for this class
         */
        private fun SerialDescriptor.toTypeName() = TypeName(
            full = this.fullName(),
            short = this.serialName.split(".").last().replace("?", "")
        )

        fun toTypeName(): TypeName {
            return descriptor.toTypeName()
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
