package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleSwaggerSchemaAnnotation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import kotlin.reflect.typeOf

@Schema(
    description = "some description",
    title = "My Test Class",
    name = "TestClass",
)
class MyTestClass(

    @field:Schema(
        description = "some value",
        title = "Some Value",
        name = "someValue",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        allowableValues = ["1", "2", "3", "4"],
        defaultValue = "1",
        accessMode = Schema.AccessMode.READ_ONLY,
        minLength = 1,
        maxLength = 2,
        format = "single-digit",
        minimum = "0",
        maximum = "0",
        exclusiveMaximum = true
    )
    val myValue: Int,

    @field:Schema(
        hidden = true,
        name = "hidden-value"
    )
    val hiddenValue: String,

    @ArraySchema(
        minItems = 0,
        maxItems = 10,
        uniqueItems = true
    )
    val someTags: List<String>
)


fun main() {

    val result = typeOf<MyTestClass>()
        .processReflection()
        .generateSwaggerSchema()
        .handleSwaggerSchemaAnnotation()
        .compileInlining()

    val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()

    println(json.writeValueAsString(result.swagger))
    println()
    result.componentSchemas.forEach { (key, schema) ->
        println(key + ": " + json.writeValueAsString(schema))
        println()
    }

}

