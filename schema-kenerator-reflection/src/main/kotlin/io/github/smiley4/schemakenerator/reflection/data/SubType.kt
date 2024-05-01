package io.github.smiley4.schemakenerator.reflection.data

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class SubType(val type: KClass<*>)
