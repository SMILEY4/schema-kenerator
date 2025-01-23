package io.github.smiley4.schemakenerator.core.typedata

/**
 * Data for a type/class
 */
data class TypeData(
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
     * Additional information for collections (e.g. lists, arrays, sets)
     */
    var collectionData: CollectionData?,
    /**
     * Additional information for maps
     */
    var mapData: MapData?
) {

    companion object {

        fun createWildcard() = TypeData(
            id = TypeId.createWildcard(),
            identifyingName = TypeName("*", "*"),
            descriptiveName = TypeName("*", "*"),
            typeParameters = mutableListOf(),
            annotations = mutableListOf(),
            subtypes = mutableListOf(),
            supertypes = mutableListOf(),
            members = mutableListOf(),
            isInlineValue = false,
            enumData = null,
            collectionData = null,
            mapData = null
        )

        fun createPlaceholder(id: TypeId) = createPlaceholder(id, TypeName("*", "*"), TypeName("*", "*"), emptyList())

        fun createPlaceholder(
            id: TypeId,
            identifyingName: TypeName,
            descriptiveName: TypeName,
            typeParameters: List<TypeParameterData>
        ) = TypeData(
            id = id,
            identifyingName = identifyingName,
            descriptiveName = descriptiveName,
            typeParameters = typeParameters.toMutableList(),
            annotations = mutableListOf(),
            subtypes = mutableListOf(),
            supertypes = mutableListOf(),
            members = mutableListOf(),
            isInlineValue = false,
            enumData = null,
            collectionData = null,
            mapData = null
        )
    }

}

fun TypeData.findTypeParameter(name: String) = typeParameters.find { it.name == name }