package io.github.smiley4.schemakenerator.jsonschema.data

/**
 * Explicitly specify the type of a json-schema object for a class, e.g. "object", "string", "number" or some custom type.
 * @param type the type of the json-schema object (https://json-schema.org/understanding-json-schema/reference/type)
 */
@Target(
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonTypeHint(val type: String)
