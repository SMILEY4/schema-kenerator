package io.github.smiley4.schemakenerator.reflection.steps

/**
 * Type of function
 */
enum class FunctionPropertyType {
    /**
     * normal getter function, i.e name  starts with "get" or "is", takes no parameters and returns some value
     */
    GETTER,


    /**
     * getter function that takes no parameters and returns some value, but its name does not start with "get or "is"
     */
    WEAK_GETTER,


    /**
     * function that is not a [GETTER] or [WEAK_GETTER].
     */
    FUNCTION
}
