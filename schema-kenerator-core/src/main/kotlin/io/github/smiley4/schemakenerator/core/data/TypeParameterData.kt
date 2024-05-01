package io.github.smiley4.schemakenerator.core.data

/**
 * Data of a type parameter, i.e. a generic type
 */
data class TypeParameterData(
    val name: String,
    val type: TypeId,
    val nullable: Boolean
)