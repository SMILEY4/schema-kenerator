package io.github.smiley4.schemakenerator.core.data

/**
 * Data for a type/class
 */
data class TypeData(
    /**
     * the unique id of this type instance
     */
    val id: TypeId,
    /**
     * The name of this type. This identifies the general type/classification of this type.
     *
     * Example:
     *    Type data for a UUID-class: the type data should have the "UUID" as name of the class but generally classify it as a "String.
     *    In this example `identifyingName` would be representing "String" and `descriptiveName` is "UUID".
     *    In most cases however, both names are the same.
     */
    val identifyingName: TypeName,
    /**
     * A possibly more specific or descriptive name of this type.
     *
     * Example:
     *    Type data for a UUID-class: the type data should have the "UUID" as name of the class but generally classify it as a "String.
     *    In this example `identifyingName` would be representing "String" and `descriptiveName` is "UUID".
     *    In most cases however, both names are the same.
     */
    val descriptiveName: TypeName,
    /**
     * the type parameters of this (generic) type
     */
    val typeParameters: MutableList<TypeParameterData> = mutableListOf(),
    /**
     * list of annotations on this type
     */
    val annotations: MutableList<AnnotationData> = mutableListOf(),
    /**
     * the list of subtypes, i.e. types that extend this type
     */
    val subtypes: MutableList<TypeId> = mutableListOf(),
    /**
     * the list of supertype, i.e. types this type extends
     */
    val supertypes: MutableList<TypeId> = mutableListOf(),
    /**
     * list of members, e.g. properties, functions
     */
    val members: MutableList<MemberData> = mutableListOf(),
    /**
     * whether the type is an inline value class
     */
    var isInlineValue: Boolean = false,
    /**
     * Additional information for enums
     */
    var enumData: EnumData? = null,
    /**
     * Additional information for collections (e.g. lists, arrays, sets)
     */
    var collectionData: CollectionData? = null,
    /**
     * Additional information for maps
     */
    var mapData: MapData? = null
) {

    companion object {

        /**
         * Create a new [TypeData] representing a wildcard / any / *
         */
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


        /**
         * Create a new placeholder [TypeData]. This data is usually just temporary and will get replaced by the actual data.
         */
        fun createPlaceholder(id: TypeId) = createPlaceholder(id, TypeName("*", "*"), TypeName("*", "*"), emptyList())


        /**
         * Create a new placeholder [TypeData]. This data is usually just temporary and will get replaced by the actual data.
         */
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


    /**
     * whether this type data (most likely) represents an enum.
     */
    val isEnum: Boolean get() = this.enumData != null


    /**
     * whether this type data (most likely) represents a collection type (i.e. list, sets, ...).
     */
    val isCollection: Boolean get() = this.collectionData != null


    /**
     * whether this type data (most likely) represents a map type.
     */
    val isMap: Boolean get() = this.mapData != null

}
