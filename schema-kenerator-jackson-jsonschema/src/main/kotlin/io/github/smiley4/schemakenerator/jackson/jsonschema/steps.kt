package io.github.smiley4.schemakenerator.jackson.jsonschema

import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jackson.jsonschema.steps.JacksonJsonSchemaAnnotationPropertyDescriptionStep
import io.github.smiley4.schemakenerator.jsonschema.data.JsonSchema

/**
 * See [JacksonJsonSchemaAnnotationPropertyDescriptionStep]
 */
fun Bundle<JsonSchema>.handleJacksonPropertyDescriptionAnnotation(): Bundle<JsonSchema> {
    return JacksonJsonSchemaAnnotationPropertyDescriptionStep().process(this)
}