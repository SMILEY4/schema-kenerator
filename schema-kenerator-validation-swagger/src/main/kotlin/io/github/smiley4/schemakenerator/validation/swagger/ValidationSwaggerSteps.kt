package io.github.smiley4.schemakenerator.validation.swagger

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

object ValidationSwaggerSteps {

    /**
     * Adds support for the following Javax Validation annotations:
     * - [javax.validation.constraints.Max]
     * - [javax.validation.constraints.Min]
     * - [javax.validation.constraints.NotBlank]
     * - [javax.validation.constraints.NotEmpty]
     * - [javax.validation.constraints.NotNull]
     * - [javax.validation.constraints.Size]
     * Add this step after schema generation and before schema compilation.
     */
    fun Bundle<SwaggerSchema>.handleJavaxValidationAnnotations(): Bundle<SwaggerSchema> {
        return this.let { SwaggerJavaxValidationAnnotationStep().process(it) }
    }


    /**
     * Adds support for the following Jakarta Validation annotations:
     * - [jakarta.validation.constraints.Max]
     * - [jakarta.validation.constraints.Min]
     * - [jakarta.validation.constraints.NotBlank]
     * - [jakarta.validation.constraints.NotEmpty]
     * - [jakarta.validation.constraints.NotNull]
     * - [jakarta.validation.constraints.Size]
     * Add this step after schema generation and before schema compilation.
     */
    fun Bundle<SwaggerSchema>.handleJakartaValidationAnnotations(): Bundle<SwaggerSchema> {
        return this.let { SwaggerJakartaValidationAnnotationStep().process(it) }
    }

}
