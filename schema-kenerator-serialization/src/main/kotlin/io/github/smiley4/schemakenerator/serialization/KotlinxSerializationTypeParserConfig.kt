package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.parser.CustomTypeParser
import io.github.smiley4.schemakenerator.core.parser.TypeParserConfig
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