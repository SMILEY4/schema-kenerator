package io.github.smiley4.schemakenerator.test.models.kotlinx

import io.github.smiley4.schemakenerator.core.annotations.Default
import io.github.smiley4.schemakenerator.core.annotations.Deprecated
import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.annotations.Example
import io.github.smiley4.schemakenerator.core.annotations.Title
import kotlinx.serialization.Serializable

@Serializable
@Title("Annotated Class")
@Description("some description")
@Default("default value")
@Example("example 1")
@Deprecated
class CoreAnnotatedClass(
    @Description("field description")
    val value: String
)