package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.TypeParserConfig
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * Configuration for parsing kotlin types
 */
class ReflectionTypeParserConfig(
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
    val customParser: CustomReflectionTypeParser?,
    /**
     * Custom parsers for specific types, overwriting the default parser for the given type.
     */
    val customParsers: Map<KClass<*>, CustomReflectionTypeParser>,
    /**
     * Determines the [ClassType] of a given type.
     */
    val typeDecider: TypeDecider,
    /**
     * Filters deciding which properties to include and which to ignore.
     */
    val propertyFilters: List<ReflectionPropertyFilter>,
    /**
     * Types that will be treated as primitive types.
     */
    val primitiveTypes: Set<KClass<*>>,
    /**
     * Types whose supertypes will not be included.
     */
    val ignoreSupertypes: Set<KClass<*>>
) : TypeParserConfig()


/**
 * Builder for the [ReflectionTypeParserConfig]
 */
class ReflectionTypeParserConfigBuilder {

    /**
     * Automatically clear the context before parsing.
     */
    var clearContext = true


    /**
     * whether to inline types into one single data-object or keep them separated in the context for later reference.
     * Note: `true` may cause infinite loops for some types.
     */
    var inline = false


    /**
     * A custom parser for all types.
     */
    var customParser: CustomReflectionTypeParser? = null


    /**
     * The custom parsers for specific types.
     */
    private val parsers = mutableMapOf<KClass<*>, CustomReflectionTypeParser>()


    /**
     * Register a custom parser for the given type.
     * @param type the type to overwrite the default parsing behavior for
     * @param parser the custom parser for the given type
     */
    fun registerParser(type: KClass<*>, parser: CustomReflectionTypeParser) {
        parsers[type] = parser
    }


    /**
     * List of filters for properties.
     */
    private val propertyFilters = mutableListOf<ReflectionPropertyFilter>()


    /**
     * Register a new [ReflectionPropertyFilter] that determines, whether a property of a type will be included or ignored.
     */
    fun registerPropertyFilter(filter: ReflectionPropertyFilter) {
        propertyFilters.add(filter)
    }


    /**
     * include method starting with "is" or "get" and only return a value while taking no parameters.
     */
    var includeGetters = true


    /**
     * include methods that only return a value while taking no parameters.
     */
    var includeWeakGetters = false


    /**
     * include hidden (e.g. private) fields and methods.
     */
    var includeHidden = false


    /**
     * Types that will be treated as primitive and analyzed with less detail (no members, no supertypes, ...).
     */
    var primitiveTypes = mutableSetOf<KClass<*>>(
        Number::class,
        Byte::class,
        Short::class,
        Int::class,
        Long::class,
        UByte::class,
        UShort::class,
        UInt::class,
        ULong::class,
        Float::class,
        Double::class,
        Boolean::class,
        Char::class,
        String::class,
        Any::class,
        Unit::class,
    )


    /**
     * Ignore, i.e don't include supertypes of the given types.
     */
    var ignoreSupertypes: Set<KClass<*>> = mutableSetOf(
        Any::class,
        Enum::class,
        Cloneable::class,
        Serializable::class,
    )


    /**
     * Determines the [ClassType] of a given type.
     */
    var typeDecider: TypeDecider = TypeDeciderImpl()


    /**
     * Build the completed [ReflectionTypeParserConfig].
     */
    fun build(): ReflectionTypeParserConfig {
        return ReflectionTypeParserConfig(
            clearContext = clearContext,
            typeDecider = typeDecider,
            customParser = customParser,
            customParsers = parsers,
            inline = inline,
            propertyFilters = buildList {
                add(FunctionReflectionPropertyFilter())
                add(GetterReflectionPropertyFilter(includeGetters))
                add(WeakGetterReflectionPropertyFilter(includeWeakGetters))
                add(VisibilityGetterReflectionPropertyFilter(includeHidden))
            } + propertyFilters,
            primitiveTypes = primitiveTypes,
            ignoreSupertypes = ignoreSupertypes
        )
    }

}

