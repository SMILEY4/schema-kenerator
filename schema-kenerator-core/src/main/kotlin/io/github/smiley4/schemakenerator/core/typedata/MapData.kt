package io.github.smiley4.schemakenerator.core.typedata

/**
 * Additional information about maps
 */
data class MapData(
    /**
     * the type of the key
     */
    var keyType: MemberData,
    /**
     * the type of the values
     */
    var valueType: MemberData,
)