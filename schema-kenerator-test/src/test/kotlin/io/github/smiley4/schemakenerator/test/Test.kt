package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.jackson.handleJacksonAnnotations
import io.github.smiley4.schemakenerator.jackson.swagger.handleJacksonSwaggerAnnotations
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.swagger.OptionalHandling
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.validation.swagger.handleJakartaValidationAnnotations
import io.github.smiley4.schemakenerator.validation.swagger.handleJavaxValidationAnnotations
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.oas.models.media.Schema
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class Test : StringSpec({

    "test" {

        val result = typeOf<LoginRequest>()
            .processReflection()
            .handleJacksonAnnotations()
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