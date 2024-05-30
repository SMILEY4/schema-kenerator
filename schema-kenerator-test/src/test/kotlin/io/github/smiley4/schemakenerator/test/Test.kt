package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
class MyTestClass(
    val mySet: Set<Int>
)


fun main() {

    val result = typeOf<MyTestClass>()
        .processKotlinxSerialization()
        .generateJsonSchema()
        .compileInlining()

    println(result.json.prettyPrint())

}

