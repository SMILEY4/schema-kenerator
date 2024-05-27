package io.github.smiley4.schemakenerator.core.annotations

/**
 * Specifies a description of the annotated object.
 * @param description a short description
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Description(val description: String)
