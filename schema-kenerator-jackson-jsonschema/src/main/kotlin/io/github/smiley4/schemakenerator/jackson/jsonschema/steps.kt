package io.github.smiley4.schemakenerator.jackson.jsonschema

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jackson.jsonschema.steps.JacksonJsonSchemaPropertyDescriptionStep
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema

/**
 * Handles the jackson "JsonPropertyDescription"-annotation.
 * See [JacksonJsonSchemaPropertyDescriptionStep]
 */
fun Bundle<JsonSchema>.handleJacksonJsonSchemaAnnotations(): Bundle<JsonSchema> {
    return this
        .let { JacksonJsonSchemaPropertyDescriptionStep().process(this) }
}
