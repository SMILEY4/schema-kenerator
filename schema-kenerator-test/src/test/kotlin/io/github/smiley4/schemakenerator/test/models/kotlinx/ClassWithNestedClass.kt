package io.github.smiley4.schemakenerator.test.models.kotlinx

import kotlinx.serialization.Serializable

@Serializable
class ClassWithNestedClass(
    val nested: NestedClass
)

@Serializable
class NestedClass(val text: String)