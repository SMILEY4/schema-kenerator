package io.github.smiley4.schemakenerator.swagger.data

/**
 * Explicitly specify the type of a swagger-schema object for a class, e.g. "object", "string", "number" or some custom type.
 * @param type the type of the json-schema object (https://swagger.io/docs/specification/data-models/data-types/)
 */
@Target(
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class SwaggerTypeHint(val type: String)
