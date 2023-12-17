package io.github.smiley4.schemakenerator.parser.reflection

import kotlin.reflect.KClass

class EnumValueParser(private val typeParser: TypeReflectionParser) {

    fun parse(clazz: KClass<*>): List<String> {
        return clazz.java.enumConstants?.map { it.toString() } ?: emptyList()
    }

}