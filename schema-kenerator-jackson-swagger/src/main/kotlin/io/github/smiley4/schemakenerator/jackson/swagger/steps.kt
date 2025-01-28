package io.github.smiley4.schemakenerator.jackson.swagger

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

/**
 * Adds a description to properties according to the jackson [JsonPropertyDescription]-annotation.
 */
fun Bundle<SwaggerSchema>.handleJacksonSwaggerAnnotations(): Bundle<SwaggerSchema> {
    return this.let { JacksonSwaggerPropertyDescriptionStep().process(this) }
}
