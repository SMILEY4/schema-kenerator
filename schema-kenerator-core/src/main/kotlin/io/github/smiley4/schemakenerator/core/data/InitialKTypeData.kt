package io.github.smiley4.schemakenerator.core.data

import kotlin.reflect.KType

/**
 * [InitialTypeData] for [KType]s.
 */
class InitialKTypeData(
    /**
     * the root type.
     */
    val type: KType,
    /**
     * additional types associated with the root type.
     */
    val associatedTypes: List<KType>,
) : InitialTypeData {

    /**
     * [type] and [associatedTypes] combined
     */
    val types: List<KType>
        get() = listOf(type) + associatedTypes

}