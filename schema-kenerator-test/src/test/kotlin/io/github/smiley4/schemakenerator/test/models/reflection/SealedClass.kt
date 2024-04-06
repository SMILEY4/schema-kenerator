package io.github.smiley4.schemakenerator.test.models.reflection

sealed class SealedClass(
    val sealedValue: String
)

class SubClassA(val a: Int, sealedValue: String) : SealedClass(sealedValue)

class SubClassB(val b: Int, sealedValue: String) : SealedClass(sealedValue)