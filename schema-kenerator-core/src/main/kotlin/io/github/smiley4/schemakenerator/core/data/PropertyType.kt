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
     * the property is a normal getter function, i.e name  starts with "get" or "is", takes no parameters and returns some value
     */
    GETTER,


    /**
     * the property is getter function that takes no parameters and returns some value, but its name does not start with "get or "is"
     */
    WEAK_GETTER,

    /**
     * the property is a function that is not a [GETTER] or [WEAK_GETTER].
     */
    FUNCTION,
}
