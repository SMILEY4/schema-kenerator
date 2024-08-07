package io.github.smiley4.schemakenerator.core.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Specify a name for the annotated class.
 * @param name the name
 * @param qualifiedName the qualified name (optional, leave as empty string to use [name])
 */
@OptIn(ExperimentalSerializationApi::class)
@Target(
    AnnotationTarget.CLASS,
)
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
annotation class Name(val name: String, val qualifiedName: String = "")
