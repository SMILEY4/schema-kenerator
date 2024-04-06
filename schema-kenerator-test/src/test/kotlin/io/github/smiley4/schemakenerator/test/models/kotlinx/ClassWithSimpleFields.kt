package io.github.smiley4.schemakenerator.test.models.kotlinx

import kotlinx.serialization.Serializable

@Serializable
class ClassWithSimpleFields(
    val someString: String,
    val someNullableInt: Int?,
    val someBoolList: List<Boolean>,
)