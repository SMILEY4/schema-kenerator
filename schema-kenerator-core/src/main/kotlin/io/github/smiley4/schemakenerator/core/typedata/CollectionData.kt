package io.github.smiley4.schemakenerator.core.typedata

/**
 * Additional information about collections
 */
data class CollectionData(
    /**
     * the type of the items
     */
    var itemType: MemberData,
    /**
     * whether the items in the collection are unique
     */
    var unique: Boolean
)
