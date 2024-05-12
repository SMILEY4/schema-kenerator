package io.github.smiley4.schemakenerator.core.annotations


/**
 * Specifies whether the annotated object is deprecated.
 * @param deprecated whether the object is deprecated
 */
 @Target(
     AnnotationTarget.CLASS,
     AnnotationTarget.PROPERTY,
     AnnotationTarget.FIELD,
     AnnotationTarget.FUNCTION
 )
 @Retention(AnnotationRetention.RUNTIME)
annotation class SchemaDeprecated(val deprecated: Boolean = true)
