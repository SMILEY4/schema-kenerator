package io.github.smiley4.schemakenerator.parser.serialization

import io.github.smiley4.schemakenerator.parser.core.CustomTypeParser
import io.github.smiley4.schemakenerator.parser.core.TypeParserConfig
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.reflect.KClass

/**
 * Configuration for parsing kotlin types
 */
class KotlinxSerializationTypeParserConfig(
    val customParsers: Map<String, CustomTypeParser<SerialDescriptor>>,
) : TypeParserConfig()


class KotlinxSerializationTypeParserConfigBuilder {

    private val parsers = mutableMapOf<String, CustomTypeParser<SerialDescriptor>>()

    fun registerParser(type: KClass<*>, parser: CustomTypeParser<SerialDescriptor>) {
        type.qualifiedName?.also { registerParser(it, parser) }
    }

    fun registerParser(type: String, parser: CustomTypeParser<SerialDescriptor>) {
        parsers[type] = parser
    }

    fun build(): KotlinxSerializationTypeParserConfig {
        return KotlinxSerializationTypeParserConfig(
            customParsers = parsers
        )
    }

}