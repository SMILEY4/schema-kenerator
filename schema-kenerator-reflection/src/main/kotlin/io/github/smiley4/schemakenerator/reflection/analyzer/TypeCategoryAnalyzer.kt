package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.reflection.analyzer.TypeCategoryAnalyzer.TypeCategory
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

/**
 * Functions for determining the [TypeCategory] reflection.
 */
class TypeCategoryAnalyzer {

    enum class TypeCategory {
        PRIMITIVE,
        OBJECT,
        ENUM,
        COLLECTION,
        MAP
    }

    companion object {

        val DEFAULT_PRIMITIVE_TYPES = setOf<KClass<*>>(
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

    }


    /**
     * Determine the general category the given type falls into (e.g. primitive, enum, collection, ...)
     */
    fun determineTypeCategory(type: KType, primitiveTypes: Collection<KClass<*>>): TypeCategory {
        return when {
            isPrimitive(type, primitiveTypes) -> TypeCategory.PRIMITIVE
            isEnum(type) -> TypeCategory.ENUM
            isCollection(type) -> TypeCategory.COLLECTION
            isMap(type) -> TypeCategory.MAP
            else -> TypeCategory.OBJECT
        }
    }


    /**
     * @return whether the given type is a primitive type (as specified by [primitiveTypes]).
     */
    fun isPrimitive(type: KType, primitiveTypes: Collection<KClass<*>>): Boolean {
        return primitiveTypes.contains(type.classifier as KClass<*>)
    }


    /**
     * @return whether the given type is an enum
     */
    fun isEnum(type: KType): Boolean {
        return (type.classifier as KClass<*>).java.enumConstants != null
    }


    /**
     * @return whether the given type is a collection (i.e. list, array, set, ...)
     */
    fun isCollection(type: KType): Boolean {
        return if (type.isSubtypeOf(typeOf<Collection<*>>()) || type.isSubtypeOf(typeOf<Array<*>>())) {
            true
        } else {
            (type.classifier as KClass<*>).supertypes.any { isCollection(it) }
        }
    }


    /**
     * @return whether the given type is a map
     */
    fun isMap(type: KType): Boolean {
        val clazz = type.classifier as KClass<*>
        return if (clazz.qualifiedName == Map::class.qualifiedName) {
            true
        } else {
            clazz.supertypes.any { isMap(it) }
        }
    }

}
