package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.annotations.SchemaName
import io.github.smiley4.schemakenerator.core.renameTypes
import io.github.smiley4.schemakenerator.jsonschema.compileReferencingRoot
import io.github.smiley4.schemakenerator.jsonschema.data.RefType
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.withAutoTitle
import io.github.smiley4.schemakenerator.reflection.processReflection
import kotlin.reflect.typeOf

@SchemaName("TestData")
data class MyTestDataDto(val nestedValue: MyNestedDataDto<String>)


@SchemaName("NestedData")
data class MyNestedDataDto<T>(val someValue: T)

fun main() {

    val jsonSchema = typeOf<MyTestDataDto>()
        .processReflection()
        .renameTypes()
        .generateJsonSchema()
        .withAutoTitle(TitleType.SIMPLE)
        .compileReferencingRoot(RefType.SIMPLE)

    println(jsonSchema.json.prettyPrint())
    println()
    jsonSchema.definitions.forEach { (key, schema) ->
        println(key + ": " + schema.prettyPrint())
        println()
    }

}