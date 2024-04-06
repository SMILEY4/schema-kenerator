package io.github.smiley4.schemakenerator.test.models.reflection

class ClassWithNestedClass(
    val nested: NestedClass
)

class NestedClass(val text: String)