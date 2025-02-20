package io.github.smiley4.schemakenerator.jackson.jsonschema

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema

object JacksonJsonSchemaSteps {

    /**
     * Adds a description to properties according to the jackson [JsonPropertyDescription]-annotation.
     * Add this step after schema generation and before schema compilation.
     */
    fun Bundle<JsonSchema>.handleJacksonJsonSchemaAnnotations(): Bundle<JsonSchema> {
        return this.let { JacksonJsonSchemaPropertyDescriptionStep().process(this) }
    }

}
