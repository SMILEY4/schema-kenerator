package io.github.smiley4.schemakenerator.core.parser

/**
 * Base data for a type
 */
open class BaseTypeData(
    val id: TypeId,
    val simpleName: String,
    val qualifiedName: String,
    val typeParameters: Map<String, TypeParameterData>,
    val annotations: List<AnnotationData>
) {

    companion object {

        fun placeholder(id: TypeId) = BaseTypeData(
            id = id,
            simpleName = id.id,
            qualifiedName = id.id,
            typeParameters = emptyMap(),
            annotations = emptyList()
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
    typeParameters = emptyMap(),
    annotations = emptyList()
)


/**
 * Data of a primitive/simplified type
 */
class PrimitiveTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: Map<String, TypeParameterData> = emptyMap(),
    annotations: List<AnnotationData> = emptyList()
) : BaseTypeData(id, simpleName, qualifiedName, typeParameters, annotations)


/**
 * Data of an object with members (fields,methods) and inheritance information
 */
open class ObjectTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: Map<String, TypeParameterData> = emptyMap(),
    val subtypes: List<TypeRef> = emptyList(),
    val supertypes: List<TypeRef> = emptyList(),
    val members: List<PropertyData> = emptyList(),
    annotations: List<AnnotationData> = emptyList()
) : BaseTypeData(id, simpleName, qualifiedName, typeParameters, annotations)


/**
 * Data of an enum-type with information about the possible enum-values
 */
class EnumTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: Map<String, TypeParameterData> = emptyMap(),
    subtypes: List<TypeRef> = emptyList(),
    supertypes: List<TypeRef> = emptyList(),
    members: List<PropertyData> = emptyList(),
    annotations: List<AnnotationData> = emptyList(),
    val enumConstants: List<String>,
) : ObjectTypeData(id, simpleName, qualifiedName, typeParameters, subtypes, supertypes, members, annotations)


/**
 * Data of a map-object with information about key and value types
 */
class MapTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: Map<String, TypeParameterData> = emptyMap(),
    subtypes: List<TypeRef> = emptyList(),
    supertypes: List<TypeRef> = emptyList(),
    members: List<PropertyData> = emptyList(),
    annotations: List<AnnotationData> = emptyList(),
    val keyType: PropertyData,
    val valueType: PropertyData,
) : ObjectTypeData(id, simpleName, qualifiedName, typeParameters, subtypes, supertypes, members, annotations)


/**
 * Data of a collection-object with information about item type
 */
class CollectionTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: Map<String, TypeParameterData> = emptyMap(),
    subtypes: List<TypeRef> = emptyList(),
    supertypes: List<TypeRef> = emptyList(),
    members: List<PropertyData> = emptyList(),
    annotations: List<AnnotationData> = emptyList(),
    val itemType: PropertyData,
) : ObjectTypeData(id, simpleName, qualifiedName, typeParameters, subtypes, supertypes, members, annotations)

