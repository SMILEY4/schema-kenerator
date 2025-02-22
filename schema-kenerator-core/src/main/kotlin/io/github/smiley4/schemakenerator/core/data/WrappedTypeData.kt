package io.github.smiley4.schemakenerator.core.data

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
) {

    /**
     * Convert this [WrappedTypeData] into a [WrappedTypeId].
     */
    fun toWrappedTypeId() = WrappedTypeId(
        id = this.typeData.id,
        nullable = this.nullable,
    )

}
