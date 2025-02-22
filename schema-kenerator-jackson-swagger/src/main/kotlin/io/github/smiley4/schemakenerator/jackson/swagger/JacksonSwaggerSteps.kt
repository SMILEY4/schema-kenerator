package io.github.smiley4.schemakenerator.jackson.swagger

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData

object JacksonSwaggerSteps {

    /**
     * Adds a description to properties according to the jackson [JsonPropertyDescription]-annotation.
     * Add this step after schema generation and before schema compilation.
     */
    fun IntermediateSwaggerSchemaData.handleJacksonSwaggerAnnotations(): IntermediateSwaggerSchemaData {
        return this.let { JacksonSwaggerPropertyDescriptionStep().process(this) }
    }

}
