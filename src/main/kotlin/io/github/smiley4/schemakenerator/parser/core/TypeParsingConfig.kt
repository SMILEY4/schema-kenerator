package io.github.smiley4.schemakenerator.parser.core

import io.github.smiley4.schemakenerator.parser.data.MemberData
import io.github.smiley4.schemakenerator.parser.data.TypeRef
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType

/**
 * Configuration for parsing kotlin types
 */
data class TypeParsingConfig(
    val supertypeFilter: SupertypeFilter = SupertypeFilter.INCLUDE_ALL,
    val memberFilter: MemberFilter = MemberFilter.INCLUDE_ALL,
    val baseTypes: Set<KClass<*>> = BASE_TYPES
)


/**
 * Specify which supertypes to include
 */
interface SupertypeFilter {

    /**
     * Filter the type before parsing it
     * @return true to include it, false to ignore it
     */
    fun filter(type: KType): Boolean


    /**
     * Filter the type after parsing
     * @return true to include it, false to ignore it
     */
    fun filter(ref: TypeRef, context: TypeParsingContext): Boolean

    companion object {
        /**
         * Filter nothing, i.e. include all types
         */
        val INCLUDE_ALL = object : SupertypeFilter {
            override fun filter(type: KType) = true
            override fun filter(ref: TypeRef, context: TypeParsingContext) = true
        }
    }

}


/**
 * Specify which members to include
 */
interface MemberFilter {

    /**
     * Filter a property member before parsing it
     * @return true to include it, false to ignore it
     */
    fun filterProperty(property: KProperty<*>): Boolean


    /**
     * Filter a property member after parsing it
     * @return true to include it, false to ignore it
     */
    fun filterProperty(property: MemberData, context: TypeParsingContext): Boolean

    companion object {
        /**
         * Filter nothing, i.e. include all members
         */
        val INCLUDE_ALL = object : MemberFilter {
            override fun filterProperty(property: KProperty<*>) = true
            override fun filterProperty(property: MemberData, context: TypeParsingContext) = true
        }
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