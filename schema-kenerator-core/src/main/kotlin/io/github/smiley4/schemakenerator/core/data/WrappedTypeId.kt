package io.github.smiley4.schemakenerator.core.data

/**
 * [TypeId] with additional information
 */
data class WrappedTypeId(
    /**
     * the id of the actual type data
     */
    val id: TypeId,
    /**
     * Whether the type can be null
     */
    var nullable: Boolean,
)