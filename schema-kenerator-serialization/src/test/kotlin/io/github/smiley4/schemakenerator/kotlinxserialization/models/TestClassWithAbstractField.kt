package io.github.smiley4.schemakenerator.kotlinxserialization.models

import kotlinx.serialization.Serializable

@Serializable
class TestClassWithAbstractField(
    val field: TestAbstractClass
)
