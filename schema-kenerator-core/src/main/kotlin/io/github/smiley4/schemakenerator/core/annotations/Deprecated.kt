package io.github.smiley4.schemakenerator.core.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo


/**
 * Specifies whether the annotated object is deprecated.
 * @param deprecated whether the object is deprecated
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
annotation class Deprecated(val deprecated: Boolean = true)
