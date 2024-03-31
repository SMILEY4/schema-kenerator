package io.github.smiley4.schemakenerator.swagger

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
import io.github.smiley4.schemakenerator.swagger.module.InliningGenerator
import io.github.smiley4.schemakenerator.swagger.module.SwaggerAnnotationsModule
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.oas.annotations.media.Schema

class Test : StringSpec({

    "test" {

        val context = TypeDataContext()

        val parser = ReflectionTypeParser(context = context, config = { inline = true; includeGetters = true })

        val generator = SwaggerSchemaGenerator()
            .withModules(
                InliningGenerator(),
                SwaggerAnnotationsModule()
            )

        val result = parser.parse<MyTestClass>()
            .let { generator.generate(it, context) }

        println(json.writeValueAsString(result))
    }

}) {
    companion object {

        val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!


        @Schema(
            title = "TestClass - Swagger Title",
            description = "swagger class description",
            example = "{\"someValue\":\"hello\"}",
            examples = ["{\"someValue\":\"hello-A\"}", "{\"someValue\":\"hello-B\"}"],
            defaultValue = "{\"someValue\":\"default\"}",
            deprecated = true
        )
//        @SchemaTitle("Some Class")
//        @SchemaDeprecated
//        @SchemaDescription("this is some class")
//        @SchemaDefault("some default")
//        @SchemaExample("example a")
//        @SchemaExample("example b")
        class MyTestClass(
            @SchemaDeprecated
            val someValue: String = ""
        )

    }
}