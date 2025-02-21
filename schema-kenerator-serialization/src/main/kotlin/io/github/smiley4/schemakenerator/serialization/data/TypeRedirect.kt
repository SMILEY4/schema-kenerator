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
        MATCH,
        IGNORE
    }

    enum class ToNullability {
        KEEP,
        REPLACE
    }

    fun matches(original: SerialDescriptor, originalNullable: Boolean): Boolean {
        val typeMatches = original.nonNullOriginal.serialName == fromType
        val nullabilityMatches = (original.isNullable || originalNullable) == fromTypeNullable
        return when(fromNullability) {
            FromNullability.MATCH -> typeMatches && nullabilityMatches
            FromNullability.IGNORE -> typeMatches
        }
    }

    fun buildTargetType(original: SerialDescriptor, originalNullable: Boolean): Pair<SerialDescriptor?, KType?> {
        return when(toNullability) {
            ToNullability.KEEP -> if(original.isNullable || originalNullable) {
                return toType.first.let { it?.nullable } to toType.second.let { it?.withNullability(true) }
            } else {
                return toType.first.let { it?.nonNullOriginal } to toType.second.let { it?.withNullability(false) }
            }
            ToNullability.REPLACE -> toType
        }
    }

}