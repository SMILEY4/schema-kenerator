package io.github.smiley4.schemakenerator.jackson.swagger

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jackson.swagger.steps.JacksonSwaggerPropertyDescriptionStep
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Handles the jackson "JsonPropertyDescription"-annotation.
 * See [JacksonSwaggerPropertyDescriptionStep]
 */
fun Bundle<SwaggerSchema>.handleJacksonSwaggerAnnotations(): Bundle<SwaggerSchema> {
    return this
        .let { JacksonSwaggerPropertyDescriptionStep().process(this) }
}
