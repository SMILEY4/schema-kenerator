package io.github.smiley4.schemakenerator.core.data

/**
 * name for types
 */
data class TypeName(
    /**
     * full name
     */
    var full: String,
    /**
     * shorter version of the name
     */
    var short: String,
    /**
     * Name of the package, without the class-name
     */
    var packageName: String = "",
) {

    companion object {

        fun wildcard() = TypeName("*", "*", "")

    }

}
