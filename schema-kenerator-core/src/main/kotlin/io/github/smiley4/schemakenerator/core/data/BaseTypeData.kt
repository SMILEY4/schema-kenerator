package io.github.smiley4.schemakenerator.core.data

/**
 * Base data for any type
 */
sealed class BaseTypeData(
    /**
     * the id of this type
     */
    val id: TypeId,
    /**
     * a shorter, simplified name of this type
     */
    var simpleName: String,
    /**
     * the full name of this type
     */
    var qualifiedName: String,
    /**
     * the type parameters (i.e. generics) of this type by their name
     */
    var typeParameters: MutableMap<String, TypeParameterData>,
    /**
     * list of annotations on this type
     */
    var annotations: MutableList<AnnotationData>
)


/**
 * A placeholder type data. "Reserves" the given id for a yet to be resolved type.
 */
class PlaceholderTypeData(
    /**
     * the id of the type to "reserve".
     */
    id: TypeId
) : BaseTypeData(
    id = id,
    simpleName = id.full(),
    qualifiedName = id.full(),
    typeParameters = mutableMapOf(),
    annotations = mutableListOf()
)


/**
 * Data for a wildcard-type
 */
class WildcardTypeData : BaseTypeData(
    id = TypeId.wildcard(),
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
 * Data of an object with members (fields,methods) and inheritance information (usually normal classes).
 */
open class ObjectTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: MutableMap<String, TypeParameterData> = mutableMapOf(),
    annotations: MutableList<AnnotationData> = mutableListOf(),
    /**
     * the list of subtypes, i.e. types that extend this type
     */
    var subtypes: MutableList<TypeId> = mutableListOf(),
    /**
     * the list of supertype, i.e. types this type extends
     */
    var supertypes: MutableList<TypeId> = mutableListOf(),
    /**
     * list of members, e.g. properties, functions
     */
    var members: MutableList<PropertyData> = mutableListOf(),
) : BaseTypeData(id, simpleName, qualifiedName, typeParameters, annotations)


/**
 * Data of an enum-type with information about the possible enum-values
 */
class EnumTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: MutableMap<String, TypeParameterData> = mutableMapOf(),
    subtypes: MutableList<TypeId> = mutableListOf(),
    supertypes: MutableList<TypeId> = mutableListOf(),
    members: MutableList<PropertyData> = mutableListOf(),
    annotations: MutableList<AnnotationData> = mutableListOf(),
    /**
     * the possible values of the enum
     */
    var enumConstants: MutableList<String>,
) : ObjectTypeData(id, simpleName, qualifiedName, typeParameters, annotations, subtypes, supertypes, members)


/**
 * Data of a map-object with information about key and value types
 */
class MapTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: MutableMap<String, TypeParameterData> = mutableMapOf(),
    subtypes: MutableList<TypeId> = mutableListOf(),
    supertypes: MutableList<TypeId> = mutableListOf(),
    members: MutableList<PropertyData> = mutableListOf(),
    annotations: MutableList<AnnotationData> = mutableListOf(),
    /**
     * the type of the key
     */
    var keyType: PropertyData,
    /**
     * the type of the values
     */
    var valueType: PropertyData,
) : ObjectTypeData(id, simpleName, qualifiedName, typeParameters, annotations, subtypes, supertypes, members)


/**
 * Data of a collection-object with information about the item type
 */
class CollectionTypeData(
    id: TypeId,
    simpleName: String,
    qualifiedName: String,
    typeParameters: MutableMap<String, TypeParameterData> = mutableMapOf(),
    subtypes: MutableList<TypeId> = mutableListOf(),
    supertypes: MutableList<TypeId> = mutableListOf(),
    members: MutableList<PropertyData> = mutableListOf(),
    annotations: MutableList<AnnotationData> = mutableListOf(),
    /**
     * the type of the items
     */
    val itemType: PropertyData,
    /**
     * whether the items in the collection are unique
     */
    val unique: Boolean
) : ObjectTypeData(id, simpleName, qualifiedName, typeParameters, annotations, subtypes, supertypes, members)

