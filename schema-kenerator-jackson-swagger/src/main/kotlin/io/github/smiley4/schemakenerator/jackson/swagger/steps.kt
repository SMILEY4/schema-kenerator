package io.github.smiley4.schemakenerator.jackson.swagger

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jackson.swagger.steps.JacksonSwaggerPropertyDescriptionStep
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * See [JacksonSwaggerPropertyDescriptionStep]
 */
fun Bundle<SwaggerSchema>.handleJacksonPropertyDescriptionAnnotation(): Bundle<SwaggerSchema> {
    return JacksonSwaggerPropertyDescriptionStep().process(this)
}
