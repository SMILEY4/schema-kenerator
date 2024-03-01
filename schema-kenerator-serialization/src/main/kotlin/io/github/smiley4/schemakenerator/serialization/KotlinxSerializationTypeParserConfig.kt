package io.github.smiley4.schemakenerator.serialization

import io.github.smiley4.schemakenerator.core.parser.TypeParserConfig
import kotlin.reflect.KClass

/**
 * Configuration for parsing kotlin types.
 */
class KotlinxSerializationTypeParserConfig(
    /**
     * Automatically clear the context before parsing.
     */
    val clearContext: Boolean,
    /**
     * Whether to inline additional types or keep them separate in the context and reference them.
     */
    val inline: Boolean,
    /**
     * An optional custom parser for all types, overwriting the default parser logic is required.
     */
    val customParser: CustomKotlinxSerializationTypeParser?,
    /**
     * Custom parsers for specific types, overwriting the default parser for the given type.
     */
    val customParsers: Map<String, CustomKotlinxSerializationTypeParser>,
) : TypeParserConfig()


/**
 * Builder for the [KotlinxSerializationTypeParserConfig]
 */
class KotlinxSerializationTypeParserConfigBuilder {

    /**
     * Automatically clear the context before parsing.
     */
    var clearContext = true


    /**
     * Whether to inline additional types or keep them separate in the context and reference them.
     */
    var inline: Boolean = true


    /**
     * A custom parser for all types.
     */
    var customParser: CustomKotlinxSerializationTypeParser? = null


    /**
     * The custom parsers for specific types.
     */
    private val parsers = mutableMapOf<String, CustomKotlinxSerializationTypeParser>()


    /**
     * Register a custom parser for the given type.
     * @param type the type to overwrite the default parsing behavior for
     * @param parser the custom parser for the given type
     */
    fun registerParser(type: KClass<*>, parser: CustomKotlinxSerializationTypeParser) {
        type.qualifiedName?.also { registerParser(it, parser) }
    }


    /**
     * Register a custom parser for the given type.
     * @param type the qualified name of the type to overwrite the default parsing behavior for
     * @param parser the custom parser for the given type
     */
    fun registerParser(type: String, parser: CustomKotlinxSerializationTypeParser) {
        parsers[type] = parser
    }


    /**
     * Build the completed [KotlinxSerializationTypeParserConfig].
     */
    fun build(): KotlinxSerializationTypeParserConfig {
        return KotlinxSerializationTypeParserConfig(
            clearContext = clearContext,
            customParser = customParser,
            customParsers = parsers,
            inline = inline
        )
    }

}