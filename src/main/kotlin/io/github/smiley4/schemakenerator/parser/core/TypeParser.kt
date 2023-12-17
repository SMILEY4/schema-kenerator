package io.github.smiley4.schemakenerator.parser.core

import io.github.smiley4.schemakenerator.getKType
import io.github.smiley4.schemakenerator.parser.data.TypeRef
import kotlin.reflect.KType

interface TypeParser {
    fun parse(type: KType): TypeRef
    fun getContext(): TypeParsingContext
}

inline fun <reified T> TypeParser.parse(): TypeRef = this.parse(getKType<T>())