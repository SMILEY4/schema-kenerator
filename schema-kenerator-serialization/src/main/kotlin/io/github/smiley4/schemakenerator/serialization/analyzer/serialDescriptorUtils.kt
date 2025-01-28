package io.github.smiley4.schemakenerator.serialization.analyzer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor

/**
 * @return the name for this serial descriptor without modifiers (e.g. nullable)
 */
@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.fullName() = this.serialName.replace("?", "")
