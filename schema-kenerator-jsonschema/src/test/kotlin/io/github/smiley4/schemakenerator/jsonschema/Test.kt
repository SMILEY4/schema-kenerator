package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.core.annotations.SchemaExample
import io.github.smiley4.schemakenerator.core.annotations.SchemaTitle
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.jsonschema.module.AnnotationJsonSchemaGeneratorModule
import io.github.smiley4.schemakenerator.jsonschema.module.ReferencingJsonSchemaGeneratorModule
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser


@SchemaTitle("My Class - A")
@SchemaDescription("A class for testing the schema generator")
class MyAnnotatedClass(
    @SchemaTitle("someValue")
    @SchemaDescription("value for testing")
    @SchemaExample("hello")
    @SchemaExample("world")
    @SchemaDefault("something")
    @Deprecated("")
    @SchemaDeprecated(false)
    val value: String
)

data class MySimpleClass(val value: String)

@SchemaDescription("The base class")
sealed class MyBaseClass(
    @SchemaDescription("The base field")
    val value: String
)

@SchemaDescription("The first implementation")
class ImplA(val a: Int, value: String) : MyBaseClass(value)

@SchemaDescription("The other implementation")
class ImplB(val b: Boolean, value: String) : MyBaseClass(value)


fun main() {
    testAnnotations()
//    testInheritance()
//    testList()
}

fun testAnnotations() {
    val context = TypeDataContext()
    val parser = ReflectionTypeParser(context = context, config = { inline = false })

    val generator = JsonSchemaGenerator()
        .withModule(ReferencingJsonSchemaGeneratorModule(true))
        .withModule(AnnotationJsonSchemaGeneratorModule())

    val result = parser.parse<MyAnnotatedClass>()
    val schema = generator.generate(result, context)

    println(schema.asJson().prettyPrint())
}


fun testInheritance() {
    val context = TypeDataContext()
    val parser = ReflectionTypeParser(context = context, config = { inline = false })

    val generator = JsonSchemaGenerator()
        .withModule(ReferencingJsonSchemaGeneratorModule(true))
        .withModule(AnnotationJsonSchemaGeneratorModule())

    val result = parser.parse<MyBaseClass>()
    val schema = generator.generate(result, context)

    println(schema.asJson().prettyPrint())
}


fun testList() {
    val context = TypeDataContext()
    val parser = ReflectionTypeParser(context = context, config = { inline = false })

    val generator = JsonSchemaGenerator()
        .withModule(ReferencingJsonSchemaGeneratorModule(true))
        .withModule(AnnotationJsonSchemaGeneratorModule())

    val result = parser.parse<List<MySimpleClass>>()
    val schema = generator.generate(result, context)

    println(schema.asJson().prettyPrint())
}
