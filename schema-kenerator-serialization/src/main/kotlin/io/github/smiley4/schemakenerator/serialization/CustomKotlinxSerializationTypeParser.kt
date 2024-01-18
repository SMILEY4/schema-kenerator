package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.parser.CustomTypeParser
import kotlinx.serialization.descriptors.SerialDescriptor

fun interface CustomKotlinxSerializationTypeParser : CustomTypeParser<SerialDescriptor>