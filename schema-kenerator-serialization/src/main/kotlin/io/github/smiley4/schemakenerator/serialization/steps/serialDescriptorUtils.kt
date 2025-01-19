package io.github.smiley4.schemakenerator.serialization.steps

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.reflect.KClass


/**
 * @return the name for this serial descriptor without modifiers (e.g. nullable)
 */
@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.fullName() = this.serialName.replace("?", "")


/**
 * @return a shortened name for this serial descriptor without modifiers (e.g. nullable)
 */
@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.shortName() = this.serialName.split(".").last().replace("?", "")

fun SerialDescriptor.matches(type: KClass<*>) = this.fullName() == (type.qualifiedName ?: type.java.name)