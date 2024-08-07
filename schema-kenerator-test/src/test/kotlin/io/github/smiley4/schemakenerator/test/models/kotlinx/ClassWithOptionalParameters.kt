package io.github.smiley4.schemakenerator.test.models.kotlinx

import kotlinx.serialization.Serializable

@Serializable
class ClassWithOptionalParameters(
    val ctorRequired: String,
    val ctorOptional: String = "test",
    val ctorRequiredNullable: String?,
    val ctorOptionalNullable: String? = null
)