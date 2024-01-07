package io.github.smiley4.schemakenerator.core.parser

/**
 * Base data for a type
 */
open class BaseTypeData(
    val id: TypeId,
    val simpleName: String,
    val qualifiedName: String,
    val typeParameters: Map<String, TypeParameterData>,
) {

    companion object {

        fun placeholder(ref: TypeId) = BaseTypeData(
            id = ref,
            simpleName = ref.id,
            qualifiedName = ref.id,
            typeParameters = emptyMap(),
        )

    }

}


/**
 * Data for a wildcard-type
 */
class WildcardTypeData : BaseTypeData(
    id = TypeId("*"),
    simpleName = "*",
    qualifiedName = "*",
    typeParameters = emptyMap()
)


/**
 * Data of a primitive/simplified type
 */
class PrimitiveTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: Map<String, TypeParameterData> = emptyMap()
) : BaseTypeData(id, simpleName, qualifiedName, typeParameters)


/**
 * Data of an object with members (fields,methods) and inheritance information
 */
open class ObjectTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: Map<String, TypeParameterData> = emptyMap(),
    val subtypes: List<TypeId> = emptyList(),
    val supertypes: List<TypeId> = emptyList(),
    val members: List<PropertyData> = emptyList(),
) : BaseTypeData(id, simpleName, qualifiedName, typeParameters)


/**
 * Data of an enum-type with information about the possible enum-values
 */
class EnumTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: Map<String, TypeParameterData> = emptyMap(),
    subtypes: List<TypeId> = emptyList(),
    supertypes: List<TypeId> = emptyList(),
    members: List<PropertyData> = emptyList(),
    val enumConstants: List<String>
) : ObjectTypeData(id, simpleName, qualifiedName, typeParameters, subtypes, supertypes, members)


/**
 * Data of a map-object with information about key and value types
 */
class MapTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: Map<String, TypeParameterData> = emptyMap(),
    subtypes: List<TypeId> = emptyList(),
    supertypes: List<TypeId> = emptyList(),
    members: List<PropertyData> = emptyList(),
    val keyType: PropertyData,
    val valueType: PropertyData
) : ObjectTypeData(id, simpleName, qualifiedName, typeParameters, subtypes, supertypes, members)


/**
 * Data of a collection-object with information about item type
 */
class CollectionTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: Map<String, TypeParameterData> = emptyMap(),
    subtypes: List<TypeId> = emptyList(),
    supertypes: List<TypeId> = emptyList(),
    members: List<PropertyData> = emptyList(),
    val itemType: PropertyData,
) : ObjectTypeData(id, simpleName, qualifiedName, typeParameters, subtypes, supertypes, members)

