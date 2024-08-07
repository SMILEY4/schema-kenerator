package io.github.smiley4.schemakenerator.test.models.reflection

class ClassWithOptionalParameters(
    val ctorRequired: String,
    val ctorOptional: String = "test",
    val ctorRequiredNullable: String?,
    val ctorOptionalNullable: String? = null
)