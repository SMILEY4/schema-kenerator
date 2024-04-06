package io.github.smiley4.schemakenerator.test.models.kotlinx

import kotlinx.serialization.Serializable

@Serializable
class ClassWithDeepGeneric<T>(
    val value: List<T>
)