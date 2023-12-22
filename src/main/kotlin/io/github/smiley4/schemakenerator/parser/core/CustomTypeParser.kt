package io.github.smiley4.schemakenerator.parser.core

fun interface CustomTypeParser<T> {
    fun parse(typeId: TypeId, type: T): BaseTypeData
}