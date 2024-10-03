package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.OptionalHandling
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.oas.models.media.Schema
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class Test : StringSpec({

    "test" {

        // https://stackoverflow.com/questions/48111459/how-to-define-a-property-that-can-be-string-or-null-in-openapi-swagger
        // https://stackoverflow.com/questions/40920441/how-to-specify-a-property-can-be-null-or-a-reference-with-swagger

        val result = typeOf<TestClass>()
            .processReflection()
            .generateSwaggerSchema {
                optionalHandling = OptionalHandling.NON_REQUIRED
            }
            .compileInlining()
            .let {
                SwaggerResult(
                    root = it.swagger,
                    componentSchemas = it.componentSchemas
                )
            }

        println(json.writeValueAsString(result))

    }

}) {
    companion object {

        class SwaggerResult(
            val root: Schema<*>,
            val componentSchemas: Map<String, Schema<*>>
        )


        @Serializable
        data class TestClass(
            val sthNullable: String?,
            val sthOptional: Int = 4,
            val sthOptionalAnNullable: Boolean? = false,
            val sthNullableNested: NestedTestClass?
        )

        @Serializable
        data class NestedTestClass(val value: String)

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!

    }
}