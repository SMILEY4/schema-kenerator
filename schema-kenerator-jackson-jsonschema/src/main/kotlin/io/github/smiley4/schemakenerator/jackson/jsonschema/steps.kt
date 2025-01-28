package io.github.smiley4.schemakenerator.jackson.jsonschema

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema

/**
 * Adds a description to properties according to the jackson [JsonPropertyDescription]-annotation.
 */
fun Bundle<JsonSchema>.handleJacksonJsonSchemaAnnotations(): Bundle<JsonSchema> {
    return this.let { JacksonJsonSchemaPropertyDescriptionStep().process(this) }
}
