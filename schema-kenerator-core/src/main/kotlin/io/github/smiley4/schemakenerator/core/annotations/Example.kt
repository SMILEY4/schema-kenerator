package io.github.smiley4.schemakenerator.core.annotations

/**
 * Specifies an example value for the annotated object. Add annotation multiple times for multiple different example values.
 * @param example the example value as a string
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class Example(val example: String)
