package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.jsonschema.modules.JsonSchemaCompiler
import io.github.smiley4.schemakenerator.jsonschema.modules.JsonSchemaCoreAnnotationHandler
import io.github.smiley4.schemakenerator.jsonschema.modules.JsonSchemaGenerator
import io.github.smiley4.schemakenerator.reflection.ReflectionTypeProcessor
import io.github.smiley4.schemakenerator.reflection.getKType
import io.github.smiley4.schemakenerator.test.models.reflection.CoreAnnotatedClass

class MyTest {
    @SchemaDescription("my test value") val myValue: String = ""
}

fun main() {

    val result = listOf(getKType<CoreAnnotatedClass>())
        .let { ReflectionTypeProcessor().process(it) }
        .let { JsonSchemaGenerator().generate(it) }
        .let { JsonSchemaCoreAnnotationHandler().appendDescription(it) }
        .let { JsonSchemaCompiler().compileInlining(it) }
        .map { it.json.prettyPrint() }

    println(result)


}