package io.github.smiley4.schemakenerator.core.data

/**
 * Data of a type parameter, i.e. a generic type
 */
data class TypeParameterData(
    /**
     * the name of the type parameter
     */
    val name: String,
    /**
     * the id of the type parameter
     */
    val type: TypeId,
    /**
     * whether the type parameter is nullable
     */
    val nullable: Boolean
)