package io.github.smiley4.schemakenerator.test.models.reflection

@JvmInline
value class ValueClass(val inlinedValue: Int)

data class ClassWithValueClass(val myValue: ValueClass, val someText: String)
