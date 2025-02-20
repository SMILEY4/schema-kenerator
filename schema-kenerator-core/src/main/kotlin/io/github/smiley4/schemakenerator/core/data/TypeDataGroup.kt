package io.github.smiley4.schemakenerator.core.data

/**
 * All [TypeData] resulting from type analysis and processing steps.
 */
data class TypeDataGroup(
    val rootId: TypeId,
    val data: Map<TypeId, TypeData>
) {

    /**
     * all contained [TypeData]
     */
    val typeData: List<TypeData>
        get() = data.values.toList()


    /**
     * all contained [TypeId]
     */
    val typeIds: List<TypeId>
        get() = data.keys.toList()


    /**
     * the root [TypeData]
     */
    val root: TypeData
        get() = data[rootId] ?: throw IllegalStateException("${TypeDataGroup::class.qualifiedName} does not contain root with id $rootId")


    /**
     * @return the contained [TypeData] with the given [TypeId] or null.
     */
    operator fun get(typeId: TypeId): TypeData? = data[typeId]

}
