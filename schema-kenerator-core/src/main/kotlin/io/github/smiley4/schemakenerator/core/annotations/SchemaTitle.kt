package io.github.smiley4.schemakenerator.core.annotations

/**
 * Specify a title for the annotated class.
 * @param title the title
 */
@Target(
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class SchemaTitle(val title: String)
