package io.github.smiley4.schemakenerator.test.models.reflection

import io.github.smiley4.schemakenerator.core.annotations.Default
import io.github.smiley4.schemakenerator.core.annotations.Deprecated
import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.annotations.Example
import io.github.smiley4.schemakenerator.core.annotations.Title


@Title("Annotated Class")
@Description("some description")
@Default("default value")
@Example("example 1")
@Deprecated
class CoreAnnotatedClass(
    @Description("String field description")
    @Default("A default String value")
    @Example("An example of a String value")
    val stringValue: String,

    @Description("Int field description")
    @Default("1111")
    @Example("2222")
    val intValue: Int,
)