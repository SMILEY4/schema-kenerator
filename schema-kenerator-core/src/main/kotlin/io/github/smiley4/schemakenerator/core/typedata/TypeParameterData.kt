package io.github.smiley4.schemakenerator.core.typedata

/**
 * Data of a type parameter, i.e. a generic type
 */
data class TypeParameterData(
    /**
     * the name of the type parameter
     */
    var name: String,
    /**
     * the id of the type
     */
    var type: TypeId,
    /**
     * whether the type parameter is nullable
     */
    var nullable: Boolean,
)