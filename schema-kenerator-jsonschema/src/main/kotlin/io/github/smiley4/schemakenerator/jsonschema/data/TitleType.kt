package io.github.smiley4.schemakenerator.jsonschema.data

/**
 * Types of a schema title
 */
enum class TitleType {
    /**
     * Use the full name of a type as the title.
     */
    FULL,

    /**
     * Use a simple version of the name of a type as the title.
     * For nested classes, the name includes the names of the outer classes.
     */
    SIMPLE,

    /**
     * Use a simple version of the name of a type as the title.
     * For nested classes, the name does NOT include the names of the outer classes.
     */
    MINIMAL
}
