package io.github.smiley4.schemakenerator.core.annotations

/**
 * Specify a name for the annotated class.
 * @param name the name
 * @param qualifiedName the qualified name (optional, leave as empty string to use [name])
 */
@Target(
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Name(val name: String, val qualifiedName: String = "")
