package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.jackson.jsonschema.handleJacksonPropertyDescriptionAnnotation
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.reflection.processReflection
import kotlin.reflect.typeOf

class MyTestClass(
    @SchemaDescription("Core description")
    val someValue: String
)


fun main() {

    val result = typeOf<MyTestClass>()
        .processReflection()
        .generateJsonSchema()
        .handleJacksonPropertyDescriptionAnnotation()
        .compileInlining()
        .json
        .prettyPrint()

    println(result)

}