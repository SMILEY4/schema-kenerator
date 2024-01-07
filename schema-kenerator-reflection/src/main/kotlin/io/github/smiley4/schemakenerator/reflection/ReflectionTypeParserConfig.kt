package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.CustomTypeParser
import io.github.smiley4.schemakenerator.core.parser.TypeParserConfig
import kotlin.reflect.KClass

/**
 * Configuration for parsing kotlin types
 */
class ReflectionTypeParserConfig(
    val customParsers: Map<KClass<*>, CustomTypeParser<KClass<*>>>,
    val propertyFilters: List<ReflectionPropertyFilter>,
) : TypeParserConfig()


class ReflectionTypeParserConfigBuilder {

    private val parsers = mutableMapOf<KClass<*>, CustomTypeParser<KClass<*>>>()

    fun registerParser(type: KClass<*>, parser: CustomTypeParser<KClass<*>>) {
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


    fun build(): ReflectionTypeParserConfig {
        return ReflectionTypeParserConfig(
            customParsers = parsers,
            propertyFilters = buildList {
                add(FunctionReflectionPropertyFilter())
                add(GetterReflectionPropertyFilter(includeGetters))
                add(WeakGetterReflectionPropertyFilter(includeWeakGetters))
                add(VisibilityGetterReflectionPropertyFilter(includeHidden))
            } + propertyFilters,
        )
    }

}


/**
 * Types that will be analyzed with less detail (no members, supertypes, ...)
 */
val BASE_TYPES = setOf(
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

    Enum::class
)