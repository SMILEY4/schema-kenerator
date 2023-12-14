package io.github.smiley4.schemakenerator.newdata

import io.github.smiley4.schemakenerator.getKType
import kotlin.reflect.KType


data class TypeRef(
    val type: KType
) {

    companion object {
        val INVALID = TypeRef(getKType<NullValue>())
    }

}