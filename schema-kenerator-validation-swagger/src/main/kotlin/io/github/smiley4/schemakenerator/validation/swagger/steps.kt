package io.github.smiley4.schemakenerator.validation.swagger

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.validation.swagger.steps.SwaggerJavaxValidationAnnotationStep
import io.github.smiley4.schemakenerator.validation.swagger.steps.SwaggerJakartaValidationAnnotationStep


/**
 * See [SwaggerJavaxValidationAnnotationStep]
 */
fun Bundle<SwaggerSchema>.handleJavaxValidationAnnotations(): Bundle<SwaggerSchema> {
    return this
        .let { SwaggerJavaxValidationAnnotationStep().process(it) }
}


/**
 * See [SwaggerJakartaValidationAnnotationStep]
 */
fun Bundle<SwaggerSchema>.handleJakartaValidationAnnotations(): Bundle<SwaggerSchema> {
    return this
        .let { SwaggerJakartaValidationAnnotationStep().process(it) }
}
