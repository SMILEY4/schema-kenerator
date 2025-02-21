package io.github.smiley4.schemakenerator.reflection.data

import kotlin.reflect.KType
import kotlin.reflect.full.withNullability

class TypeRedirect(
    val fromType: KType,
    val fromNullability: FromNullability,
    val toType: KType,
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

    fun matches(original: KType): Boolean {
        val typeMatches = original.withNullability(false) == fromType.withNullability(false)
        val nullabilityMatches = original.isMarkedNullable == fromType.isMarkedNullable
        return when(fromNullability) {
            FromNullability.MATCH -> typeMatches && nullabilityMatches
            FromNullability.IGNORE -> typeMatches
        }
    }

    fun buildTargetType(original: KType): KType {
        return when(toNullability) {
            ToNullability.KEEP -> toType.withNullability(original.isMarkedNullable)
            ToNullability.REPLACE -> toType
        }
    }

}