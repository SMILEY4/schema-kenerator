package io.github.smiley4.schemakenerator.jackson.jsonschema

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData

object JacksonJsonSchemaSteps {

    /**
     * Adds a description to properties according to the jackson [JsonPropertyDescription]-annotation.
     * Add this step after schema generation and before schema compilation.
     */
    fun IntermediateJsonSchemaData.handleJacksonJsonSchemaAnnotations(): IntermediateJsonSchemaData {
        return this.let { JacksonJsonSchemaPropertyDescriptionStep().process(it) }
    }

}
