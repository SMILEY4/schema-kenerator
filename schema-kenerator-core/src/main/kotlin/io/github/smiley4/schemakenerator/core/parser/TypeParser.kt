package io.github.smiley4.schemakenerator.core.parser

import kotlin.reflect.KType

abstract class TypeParser<T : TypeParserConfig>(
    val config: T,
    val context: TypeDataContext,
) {
    abstract fun parse(type: KType): TypeRef
}