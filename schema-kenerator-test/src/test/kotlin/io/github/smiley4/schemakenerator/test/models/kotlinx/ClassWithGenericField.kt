package io.github.smiley4.schemakenerator.test.models.kotlinx

import kotlinx.serialization.Serializable

@Serializable
class ClassWithGenericField<T>(
    val value: T
)