package io.github.smiley4.schemakenerator.core.typedata

/**
 * Data for a type/class
 */
class TypeData(
    /**
     * the unique id of this type instance
     */
    val id: TypeId,
    /**
     * the name of this type.
     */
    val identifyingName: TypeName,
    /**
     * A possibly more descriptive name of this type.
     */
    val descriptiveName: TypeName,
    /**
     * the type parameters (i.e. generics) of this type
     */
    val typeParameters: MutableList<TypeParameterData>,
    /**
     * list of annotations on this type
     */
    val annotations: MutableList<AnnotationData>,
    /**
     * the list of subtypes, i.e. types that extend this type
     */
    val subtypes: MutableList<TypeId>,
    /**
     * the list of supertype, i.e. types this type extends
     */
    val supertypes: MutableList<TypeId>,
    /**
     * list of members, e.g. properties, functions
     */
    val members: MutableList<MemberData>,
    /**
     * whether the type is an inline value class
     */
    var isInlineValue: Boolean,
    /**
     * Additional information for enums
     */
    var enumData: EnumData?,
    /**
     * Additional information for collections
     */
    var collectionData: CollectionData?,
    /**
     * Additional information for maps
     */
    var mapData: MapData?
)
