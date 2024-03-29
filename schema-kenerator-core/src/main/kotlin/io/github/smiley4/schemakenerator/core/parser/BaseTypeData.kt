package io.github.smiley4.schemakenerator.core.parser

/**
 * Base data for a type
 */
sealed class BaseTypeData(
    val id: TypeId,
    var simpleName: String,
    var qualifiedName: String,
    var typeParameters: MutableMap<String, TypeParameterData>,
    var annotations: MutableList<AnnotationData>
) {

    companion object {

        fun placeholder(id: TypeId) = PlaceholderTypeData(id)

    }

}


/**
 * Data for a wildcard-type
 */
class PlaceholderTypeData(
    id: TypeId
) : BaseTypeData(
    id = id,
    simpleName = id.id,
    qualifiedName = id.id,
    typeParameters = mutableMapOf(),
    annotations = mutableListOf()
)


/**
 * Data for a wildcard-type
 */
class WildcardTypeData : BaseTypeData(
    id = TypeId("*"),
    simpleName = "*",
    qualifiedName = "*",
    typeParameters = mutableMapOf(),
    annotations = mutableListOf()
)


/**
 * Data of a primitive/simplified type
 */
class PrimitiveTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: MutableMap<String, TypeParameterData> = mutableMapOf(),
    annotations: MutableList<AnnotationData> = mutableListOf()
) : BaseTypeData(id, simpleName, qualifiedName, typeParameters, annotations)


/**
 * Data of an object with members (fields,methods) and inheritance information
 */
open class ObjectTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: MutableMap<String, TypeParameterData> = mutableMapOf(),
    var subtypes: MutableList<TypeRef> = mutableListOf(),
    var supertypes: MutableList<TypeRef> = mutableListOf(),
    var members: MutableList<PropertyData> = mutableListOf(),
    annotations: MutableList<AnnotationData> = mutableListOf()
) : BaseTypeData(id, simpleName, qualifiedName, typeParameters, annotations)


/**
 * Data of an enum-type with information about the possible enum-values
 */
class EnumTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: MutableMap<String, TypeParameterData> = mutableMapOf(),
    subtypes: MutableList<TypeRef> = mutableListOf(),
    supertypes: MutableList<TypeRef> = mutableListOf(),
    members: MutableList<PropertyData> = mutableListOf(),
    annotations: MutableList<AnnotationData> = mutableListOf(),
    var enumConstants: MutableList<String>,
) : ObjectTypeData(id, simpleName, qualifiedName, typeParameters, subtypes, supertypes, members, annotations)


/**
 * Data of a map-object with information about key and value types
 */
class MapTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: MutableMap<String, TypeParameterData> = mutableMapOf(),
    subtypes: MutableList<TypeRef> = mutableListOf(),
    supertypes: MutableList<TypeRef> = mutableListOf(),
    members: MutableList<PropertyData> = mutableListOf(),
    annotations: MutableList<AnnotationData> = mutableListOf(),
    var keyType: PropertyData,
    var valueType: PropertyData,
) : ObjectTypeData(id, simpleName, qualifiedName, typeParameters, subtypes, supertypes, members, annotations)


/**
 * Data of a collection-object with information about item type
 */
class CollectionTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: MutableMap<String, TypeParameterData> = mutableMapOf(),
    subtypes: MutableList<TypeRef> = mutableListOf(),
    supertypes: MutableList<TypeRef> = mutableListOf(),
    members: MutableList<PropertyData> = mutableListOf(),
    annotations: MutableList<AnnotationData> = mutableListOf(),
    val itemType: PropertyData,
) : ObjectTypeData(id, simpleName, qualifiedName, typeParameters, subtypes, supertypes, members, annotations)

