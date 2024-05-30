package io.github.smiley4.schemakenerator.core.annotations

/**
 * Specifies a default value for the annotated object.
 */
 @Target(
     AnnotationTarget.CLASS,
     AnnotationTarget.PROPERTY,
     AnnotationTarget.FIELD,
     AnnotationTarget.FUNCTION
 )
 @Retention(AnnotationRetention.RUNTIME)
annotation class Default(val default: String)
