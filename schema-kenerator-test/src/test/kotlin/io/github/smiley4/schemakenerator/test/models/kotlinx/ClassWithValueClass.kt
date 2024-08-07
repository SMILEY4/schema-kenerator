package io.github.smiley4.schemakenerator.test.models.kotlinx

import kotlinx.serialization.Serializable


@JvmInline
@Serializable
value class ValueClass(val inlinedValue: Int)

@Serializable
data class ClassWithValueClass(val myValue: ValueClass, val someText: String)
