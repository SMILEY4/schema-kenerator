package io.github.smiley4.schemakenerator.core.parser

fun interface CustomTypeParser<T> {
    fun parse(typeId: TypeId, type: T): BaseTypeData? // todo: pass "parent" typeParser as arg ?
}