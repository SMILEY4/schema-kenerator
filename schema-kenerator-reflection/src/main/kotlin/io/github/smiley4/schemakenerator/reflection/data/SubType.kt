package io.github.smiley4.schemakenerator.reflection.data

import kotlin.reflect.KClass

/**
 * Specifies subtype(s) of the annotated class.
 * @param type one possible subtype
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class SubType(val type: KClass<*>)
