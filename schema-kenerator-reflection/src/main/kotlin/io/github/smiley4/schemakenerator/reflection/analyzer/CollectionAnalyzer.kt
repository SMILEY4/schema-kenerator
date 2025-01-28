package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.MemberKind
import io.github.smiley4.schemakenerator.core.data.TypeParameterData
import io.github.smiley4.schemakenerator.core.data.Visibility
import io.github.smiley4.schemakenerator.core.data.find
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

/**
 * Analysis functions for collections (list, arrays, maps, ...) using reflection.
 */
class CollectionAnalyzer {

    /**
     * @param type the type to check
     * @return whether the give collection type may contain only unique items (i.e. whether it is a set)
     */
    fun areCollectionItemsUnique(type: KType): Boolean {
        return type.isSubtypeOf(typeOf<Set<*>>())
    }


    /**
     * Chooses the (best matching) type parameter for the items from the list of available type parameters.
     * @param typeParameters the list of available type parameters
     * @param fallback a fallback in case no matching type parameter could be found. Takes the name of the type parameter as input.
     * @return the [MemberData] representing the "item" type of a collection
     */
    fun getCollectionItemType(typeParameters: List<TypeParameterData>, fallback: (name: String) -> TypeParameterData): MemberData {
        val typeParameter = typeParameters.find("E")
            ?: typeParameters.find("T")
            ?: typeParameters.firstOrNull()
            ?: fallback("item")
        return MemberData(
            name = "item",
            type = typeParameter.type,
            nullable = typeParameter.nullable,
            optional = false,
            visibility = Visibility.PUBLIC,
            kind = MemberKind.PROPERTY,
            annotations = mutableListOf()
        )
    }


    /**
     * Chooses the (best matching) type parameter for the keys from the list of available type parameters.
     * @param typeParameters the list of available type parameters
     * @param fallback a fallback in case no matching type parameter could be found. Takes the name of the type parameter as input.
     * @return the [MemberData] representing the "key" type of a map
     */
    fun getMapKeyType(typeParameters: List<TypeParameterData>, fallback: (name: String) -> TypeParameterData): MemberData {
        val typeParameter = typeParameters.find("K") ?: fallback("key")
        return MemberData(
            name = "key",
            type = typeParameter.type,
            nullable = typeParameter.nullable,
            optional = false,
            visibility = Visibility.PUBLIC,
            kind = MemberKind.PROPERTY,
            annotations = mutableListOf()
        )
    }


    /**
     * Chooses the (best matching) type parameter for the values from the list of available type parameters.
     * @param typeParameters the list of available type parameters
     * @param fallback a fallback in case no matching type parameter could be found. Takes the name of the type parameter as input.
     * @return the [MemberData] representing the "value" type of a map
     */
    fun getMapValueType(typeParameters: List<TypeParameterData>, fallback: (name: String) -> TypeParameterData): MemberData {
        val typeParameter = typeParameters.find("V") ?: fallback("value")
        return MemberData(
            name = "value",
            type = typeParameter.type,
            nullable = typeParameter.nullable,
            optional = false,
            visibility = Visibility.PUBLIC,
            kind = MemberKind.PROPERTY,
            annotations = mutableListOf()
        )
    }

}
