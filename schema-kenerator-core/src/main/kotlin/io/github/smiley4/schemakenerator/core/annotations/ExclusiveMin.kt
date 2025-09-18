package io.github.smiley4.schemakenerator.core.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Specified an exclusive minimum numeric value.
 * @param value the min value (exclusive).
 */
@OptIn(ExperimentalSerializationApi::class)
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION
)
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
annotation class ExclusiveMin(val value: Long)
