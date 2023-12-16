package io.github.smiley4.schemakenerator.models


class TestClassSimple(
    val someField: String
)

enum class TestEnum {
    RED, GREEN, BLUE
}

class TestClassWithEnumField(
    val value: TestEnum
)

class TestClassWithFunctionField(
    val value: (x: Int) -> String
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


data class TestClassRecursiveGeneric<T>(
    val value: T
) : TestInterfaceRecursiveGeneric<TestClassRecursiveGeneric<*>>


interface TestInterfaceRecursiveGeneric<T>
