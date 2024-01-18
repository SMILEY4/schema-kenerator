package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.parser.TypeParserConfig
import kotlin.reflect.KClass

/**
 * Configuration for parsing kotlin types
 */
class KotlinxSerializationTypeParserConfig(
    val customParser: CustomKotlinxSerializationTypeParser?,
    val customParsers: Map<String, CustomKotlinxSerializationTypeParser>,
    val inline: Boolean,
) : TypeParserConfig()


class KotlinxSerializationTypeParserConfigBuilder {

    var inline: Boolean = true

    var customParser: CustomKotlinxSerializationTypeParser? = null

    private val parsers = mutableMapOf<String, CustomKotlinxSerializationTypeParser>()

    fun registerParser(type: KClass<*>, parser: CustomKotlinxSerializationTypeParser) {
        type.qualifiedName?.also { registerParser(it, parser) }
    }

    fun registerParser(type: String, parser: CustomKotlinxSerializationTypeParser) {
        parsers[type] = parser
    }

    fun build(): KotlinxSerializationTypeParserConfig {
        return KotlinxSerializationTypeParserConfig(
            customParser = customParser,
            customParsers = parsers,
            inline = inline
        )
    }

}