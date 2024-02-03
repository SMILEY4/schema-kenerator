package io.github.smiley4.schemakenerator.reflection.parsers

import kotlin.reflect.KClass

class EnumValueParser(private val typeParser: ReflectionTypeParser) {

    fun parse(clazz: KClass<*>): List<String> {
        return clazz.java.enumConstants?.map { it.toString() } ?: emptyList()
    }

}