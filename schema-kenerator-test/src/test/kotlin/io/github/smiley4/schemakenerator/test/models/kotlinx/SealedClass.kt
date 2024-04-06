package io.github.smiley4.schemakenerator.test.models.kotlinx

import kotlinx.serialization.Serializable

@Serializable
sealed class SealedClass {
    abstract val sealedValue: String
}

@Serializable
class SubClassA(val a: Int, override val sealedValue: String) : SealedClass()

@Serializable
class SubClassB(val b: Int, override val sealedValue: String) : SealedClass()