package io.github.smiley4.schemakenerator.core.annotations


/**
 * Specifies that the annotated object is required.
 */
 @Target(
     AnnotationTarget.PROPERTY,
     AnnotationTarget.FIELD,
     AnnotationTarget.FUNCTION
 )
 @Retention(AnnotationRetention.RUNTIME)
annotation class Required
