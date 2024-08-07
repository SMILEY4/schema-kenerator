package io.github.smiley4.schemakenerator.core.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Specify a title for the annotated class.
 * @param title the title
 */
@OptIn(ExperimentalSerializationApi::class)
@Target(
    AnnotationTarget.CLASS,
)
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
annotation class Title(val title: String)
