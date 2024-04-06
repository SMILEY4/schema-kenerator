package io.github.smiley4.schemakenerator.test.models.kotlinx

import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.core.annotations.SchemaExample
import io.github.smiley4.schemakenerator.core.annotations.SchemaTitle
import kotlinx.serialization.Serializable

@Serializable
@SchemaTitle("Annotated Class")
@SchemaDescription("some description")
//@SchemaDefault("{\"value\": \"test\"}")
//@SchemaExample("{\"value\": \"example 1\"}")
//@SchemaExample("{\"value\": \"example 2\"}")
@SchemaDefault("default value")
@SchemaExample("example 1")
@SchemaExample("example 2")
@SchemaDeprecated
class CoreAnnotatedClass(
    @SchemaDescription("field description")
    val value: String
)