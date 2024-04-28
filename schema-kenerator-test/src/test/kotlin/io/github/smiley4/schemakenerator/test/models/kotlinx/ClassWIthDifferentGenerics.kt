package io.github.smiley4.schemakenerator.test.models.kotlinx

import kotlinx.serialization.Serializable

@Serializable
class ClassWIthDifferentGenerics(
    val valueInt: ClassWithGenericField<Int>,
    val valueString: ClassWithGenericField<String>
)