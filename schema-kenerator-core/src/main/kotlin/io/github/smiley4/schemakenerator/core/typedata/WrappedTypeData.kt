package io.github.smiley4.schemakenerator.core.typedata

/**
 * [TypeData] with additional information
 */
data class WrappedTypeData(
    /**
     * the actual type data
     */
    val typeData: TypeData,
    /**
     * Whether the type can be null
     */
    var nullable: Boolean,
)
