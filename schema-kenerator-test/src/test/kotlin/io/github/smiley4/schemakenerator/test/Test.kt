package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.core.mergeGetters
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.handleDescriptionAnnotation
import io.github.smiley4.schemakenerator.reflection.processReflection
import kotlin.reflect.typeOf

class MyTestClass(
    val myNumber: Int,
    @SchemaDescription("some test value")
    private val someValue: String
) {

    fun getSomeValue(): String = someValue

}




fun main() {

    val result = typeOf<MyTestClass>()
        .processReflection {
            includeGetters = true
            includeHidden = true
        }
        .mergeGetters()
        .generateJsonSchema()
        .handleDescriptionAnnotation()
        .compileInlining()
        .json
        .prettyPrint()

    println(result)

}