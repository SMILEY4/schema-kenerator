package io.github.smiley4.schemakenerator.kotlinxserialization.models

import kotlinx.serialization.Serializable

@Serializable
data class TestClassRecursiveGeneric<T>(
    val value: T
) : TestInterfaceRecursiveGeneric<TestClassRecursiveGeneric<*>>
