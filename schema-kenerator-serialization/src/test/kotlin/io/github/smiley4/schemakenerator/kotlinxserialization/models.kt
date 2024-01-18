package io.github.smiley4.schemakenerator.kotlinxserialization

import kotlinx.serialization.Serializable

@Serializable
class TestClassSimple(
    val someField: String?
)


@Serializable
class TestClassMixedTypes(
    val myList: List<TestClassSimple>,
    val myMap: Map<String, TestClassSimple>,
    val myArray: Array<TestClassSimple>
)


@Serializable
enum class TestEnum {
    RED, GREEN, BLUE
}


@Serializable
class TestClassWithEnumField(
    val value: TestEnum
)


@Serializable
class TestClassWithFunctionField(
    val value: (x: Int) -> String
)


@Serializable
abstract class TestAbstractClass(val myField: String)


@Serializable
class TestClassWithAbstractField(
    val field: TestAbstractClass
)


@Serializable
data class TestClassGeneric<T>(
    val genericValue: T
)


@Serializable
data class TestClassDeepGeneric(
    val myInt: TestClassGeneric<Int>,
    val myString: TestClassGeneric<String>
)


@Serializable
data class TestClassRecursiveGeneric<T>(
    val value: T
) : TestInterfaceRecursiveGeneric<TestClassRecursiveGeneric<*>>

interface TestInterfaceRecursiveGeneric<T>
