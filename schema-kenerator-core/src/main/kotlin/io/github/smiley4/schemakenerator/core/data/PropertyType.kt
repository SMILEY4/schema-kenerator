package io.github.smiley4.schemakenerator.core.data

/**
 * The type of the property/member of a type
 */
enum class PropertyType {

    /**
     * the property is a field / kotlin-property
     */
    PROPERTY,


    /**
     * the property is a function
     */
    FUNCTION,
}
