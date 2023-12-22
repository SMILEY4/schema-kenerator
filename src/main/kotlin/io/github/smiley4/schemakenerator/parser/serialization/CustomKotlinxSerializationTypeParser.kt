package io.github.smiley4.schemakenerator.parser.serialization

import io.github.smiley4.schemakenerator.parser.core.CustomTypeParser
import kotlinx.serialization.descriptors.SerialDescriptor

interface CustomKotlinxSerializationTypeParser : CustomTypeParser<SerialDescriptor>