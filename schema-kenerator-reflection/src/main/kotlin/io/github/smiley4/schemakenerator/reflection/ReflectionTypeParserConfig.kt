package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.TypeParserConfig
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * Configuration for parsing kotlin types
 */
class ReflectionTypeParserConfig(
    val typeDecider: TypeDecider,
    val customParser: CustomReflectionTypeParser?,
    val customParsers: Map<KClass<*>, CustomReflectionTypeParser>,
    val propertyFilters: List<ReflectionPropertyFilter>,
    val inline: Boolean,
    val primitiveTypes: Set<KClass<*>>,
    val ignoreSupertypes: Set<KClass<*>>
) : TypeParserConfig()


class ReflectionTypeParserConfigBuilder {

    var inline = false

    var customParser: CustomReflectionTypeParser? = null

    private val parsers = mutableMapOf<KClass<*>, CustomReflectionTypeParser>()

    fun registerParser(type: KClass<*>, parser: CustomReflectionTypeParser) {
        parsers[type] = parser
    }

    private val propertyFilters = mutableListOf<ReflectionPropertyFilter>()

    fun registerPropertyFilter(filter: ReflectionPropertyFilter) {
        propertyFilters.add(filter)
    }


    /**
     * include method starting with "is" or "get" and only return a value while taking no parameters
     */
    var includeGetters = false


    /**
     * include methods that only return a value while taking no parameters
     */
    var includeWeakGetters = false


    /**
     * include hidden fields and methods
     */
    var includeHidden = false


    /**
     * Types that will be treated as primitive and analyzed with less detail (no members, supertypes, ...)
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

    var skipCollectionDetails: Boolean = true

    var ignoreSupertypes: Set<KClass<*>> = mutableSetOf(
        Any::class,
        Enum::class,
        Cloneable::class,
        Serializable::class,
        Map::class,
        List::class,
        Array::class
    )

    fun build(): ReflectionTypeParserConfig {
        return ReflectionTypeParserConfig(
            typeDecider = TypeDeciderImpl(),
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

