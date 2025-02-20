package io.github.smiley4.schemakenerator.jackson.swagger

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema

object JacksonSwaggerSteps {

    /**
     * Adds a description to properties according to the jackson [JsonPropertyDescription]-annotation.
     * Add this step after schema generation and before schema compilation.
     */
    fun Bundle<SwaggerSchema>.handleJacksonSwaggerAnnotations(): Bundle<SwaggerSchema> {
        return this.let { JacksonSwaggerPropertyDescriptionStep().process(this) }
    }

}
