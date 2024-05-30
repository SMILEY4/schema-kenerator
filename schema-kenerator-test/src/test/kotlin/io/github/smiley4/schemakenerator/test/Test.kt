package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.reflection.processReflection
import kotlin.reflect.typeOf

class MyTestClass(
    val myValue: Array<Int>
)


fun main() {

    val result = typeOf<MyTestClass>()
        .processReflection()
        .generateJsonSchema()
        .compileInlining()

    println(result.json.prettyPrint())

}

