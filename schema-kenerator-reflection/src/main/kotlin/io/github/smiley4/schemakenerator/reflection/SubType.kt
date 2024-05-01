package io.github.smiley4.schemakenerator.reflection

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class SubType(val type: KClass<*>)
