package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
import io.github.smiley4.schemakenerator.core.annotations.SchemaExample
import io.github.smiley4.schemakenerator.core.annotations.SchemaTitle
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.jsonschema.module.AnnotationJsonSchemaGeneratorModule
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser


@SchemaTitle("My Class - A")
@SchemaDescription("A class for testing the schema generator")
class MyClassA(
    @SchemaTitle("someValue")
    @SchemaDescription("value for testing")
    @SchemaExample("hello")
    @SchemaExample("world")
    @SchemaDefault("something")
    @Deprecated("")
    @SchemaDeprecated(false)
    val value: String
)


fun main() {
    val context = TypeParserContext()
    val parser = ReflectionTypeParser(context = context, config = { inline = false })
    val generator = JsonSchemaGenerator()
        .withModule(AnnotationJsonSchemaGeneratorModule())

    val result = parser.parse<MyClassA>()
    val schema = generator.generate(result, context)

    println(schema.prettyPrint())

}