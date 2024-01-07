package io.github.smiley4.schemakenerator.reflection

import java.time.LocalDateTime


class TestClassSimple(
    val someField: String
)

class TestClassMixedTypes(
    val myList: List<TestClassSimple>,
    val myMap: Map<String, TestClassSimple>,
    val myArray: Array<TestClassSimple>
)

class TestClassLocalDateTime (
    val timestamp: LocalDateTime
)

class TestClassWithMethods(
    val someText: String,
    val myFlag: Boolean,
    val isEnabled: Boolean,
) {
    fun calculateValue(): Int = 4
    fun myFlag() = myFlag.toString()
    fun isDisabled() = !isEnabled
}

enum class TestEnum {
    RED, GREEN, BLUE
}

class TestClassWithEnumField(
    val value: TestEnum
)

class TestClassWithFunctionField(
    val value: (x: Int) -> String
)

abstract class TestAbstractClass(val myField: String)


class TestClassWithAbstractField(
    val field: TestAbstractClass
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
