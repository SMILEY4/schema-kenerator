package io.github.smiley4.schemakenerator.serialization.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nonNullOriginal
import kotlinx.serialization.descriptors.nullable
import kotlin.reflect.KType
import kotlin.reflect.full.withNullability

@OptIn(ExperimentalSerializationApi::class)
class TypeRedirect(
    val fromType: String,
    val fromTypeNullable: Boolean,
    val fromNullability: FromNullability,
    val toType: Pair<SerialDescriptor?, KType?>,
    val toNullability: ToNullability,
) {

    enum class FromNullability {
        /**
         * The nullability of a potential type to replace must match the specified type.
         */
        MATCH,


        /**
         * The nullability of a potential type to replace is ignored when matching.
         */
        IGNORE
    }

    enum class ToNullability {
        /**
         * The nullability of the original replaced type is kept, i.e. only the type is replaced, not the nullability
         */
        KEEP,


        /**
         * The nullability of the original replaced type is overwritten, i.e. the type and nullability is replaced
         */
        REPLACE
    }

    fun matches(original: SerialDescriptor): Boolean {
        val typeMatches = original.nonNullOriginal.serialName == fromType
        val nullabilityMatches = original.isNullable == fromTypeNullable
        return when (fromNullability) {
            FromNullability.MATCH -> typeMatches && nullabilityMatches
            FromNullability.IGNORE -> typeMatches
        }
    }

    fun buildTargetType(original: SerialDescriptor): Pair<SerialDescriptor?, KType?> {
        return when (toNullability) {
            ToNullability.KEEP -> if (original.isNullable) {
                return toType.first.let { it?.nullable } to toType.second.let { it?.withNullability(true) }
            } else {
                return toType.first.let { it?.nonNullOriginal } to toType.second.let { it?.withNullability(false) }
            }
            ToNullability.REPLACE -> toType
        }
    }

}
