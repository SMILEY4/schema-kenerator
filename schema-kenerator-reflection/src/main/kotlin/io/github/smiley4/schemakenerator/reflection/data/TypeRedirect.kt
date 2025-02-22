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
