package io.github.smiley4.schemakenerator.core.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Specifies an example value for the annotated object. Add annotation multiple times for multiple different example values.
 * @param example the example value as a string
 */
@OptIn(ExperimentalSerializationApi::class)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION
)
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class Example(val example: String)
