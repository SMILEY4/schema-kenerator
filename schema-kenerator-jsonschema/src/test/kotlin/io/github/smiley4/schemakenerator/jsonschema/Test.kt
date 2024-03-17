package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser


data class TestData(
    val value: Long?,
    val names: Array<String>,
    val flag: Boolean,
    val obj: NestedData
)


data class NestedData(
    val nestedField: String
)

class GenericClass<T>(val value: T)


sealed class MyAbstractClass(val value: String)

class MyClassA(val a: Int) : MyAbstractClass("a")
class MyClassB(val b: Boolean) : MyAbstractClass("b")


fun main() {
    val context = TypeParserContext()
    val parser = ReflectionTypeParser(context = context, config = { inline = false})
    val generator = JsonSchemaGenerator()

    val result = parser.parse<MyAbstractClass>()
    val schema = generator.generate(result, context)

    println(schema.prettyPrint())

}