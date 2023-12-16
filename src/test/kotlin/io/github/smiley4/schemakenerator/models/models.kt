package io.github.smiley4.schemakenerator.models


class TestClassSimple(
    val someField: String
)

data class TestClassGeneric<T>(
    val value: T
)

data class TestClassDeepGeneric<E>(
    val myValues: List<E>
)


open class TestOpenClass(
    val baseField: String
)

class TestSubClass(
    baseField: String,
    val additionalField: Int
) : TestOpenClass(baseField)






