package io.github.smiley4.schemakenerator.jackson.jsonschema

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jackson.jsonschema.steps.JacksonJsonSchemaPropertyDescriptionStep
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema

/**
 * See [JacksonJsonSchemaPropertyDescriptionStep]
 */
fun Bundle<JsonSchema>.handleJacksonPropertyDescriptionAnnotation(): Bundle<JsonSchema> {
    return JacksonJsonSchemaPropertyDescriptionStep().process(this)
}
