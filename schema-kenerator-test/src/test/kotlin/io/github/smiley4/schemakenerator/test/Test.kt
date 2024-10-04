package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.validation.swagger.handleJavaxValidationAnnotations
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.oas.models.media.Schema
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class Test : StringSpec({

    "test" {
        val result = typeOf<Validated>()
            .processReflection()
            .generateSwaggerSchema()
            .handleJavaxValidationAnnotations()
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

        private class Validated(
            @field:Min(5)
            @field:Max(10)
            val minMax: Int,
            @field:NotNull
            val mustNotBeNull: Any?,
            @field:NotEmpty
            val mustNotBeEmpty: String?,
            @field:NotBlank
            val mustNotBeBlank: String?,
            @field:Size(min = 4, max = 95)
            val hasSize: String
        )


        @JsonDeserialize
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class LoginRequest(
            @field:NotBlank
            @field:Size(max = 100)
            @JsonProperty("usernameRenamed", required = true)
            val username: String?,

            @field:NotBlank
            @field:Size(max = 100)
            @JsonProperty("passwordRenamed", required = true)
            val password: String?
        )

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!

    }
}