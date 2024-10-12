package io.github.smiley4.schemakenerator.test.models.reflection

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    minimum = "5",
    maximum = "15",
    defaultValue = "default on type",
    description = "annotated value class for testing."
)
@JvmInline
value class AnnotatedValueClass(val inlinedValue: String)

data class ClassWithAnnotatedValueClass(
    @Schema(
        defaultValue = "default on property",
    )
    val myValue: AnnotatedValueClass,
)
