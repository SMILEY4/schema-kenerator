package io.github.smiley4.schemakenerator.core.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Specifies an external schema for this type.
 * @param url the url to the external schema
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
annotation class Ref(val url: String)
