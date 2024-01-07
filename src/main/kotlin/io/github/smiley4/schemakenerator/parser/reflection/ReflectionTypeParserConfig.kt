package io.github.smiley4.schemakenerator.parser.reflection

import io.github.smiley4.schemakenerator.parser.core.CustomTypeParser
import io.github.smiley4.schemakenerator.parser.core.TypeParserConfig
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

    var includeGetters = false

    var includeWeakGetters = false

    var includeAllFunctions = false

    var includeHidden = false

    fun build(): ReflectionTypeParserConfig {
        return ReflectionTypeParserConfig(
            customParsers = parsers,
            propertyFilters = buildList {
//                add(VisibilityReflectionPropertyFilter(includeHidden))
                add(FunctionReflectionPropertyFilter(includeAllFunctions))
                add(WeakGetterReflectionPropertyFilter(includeWeakGetters))
                add(TrueGetterReflectionPropertyFilter(includeGetters))
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