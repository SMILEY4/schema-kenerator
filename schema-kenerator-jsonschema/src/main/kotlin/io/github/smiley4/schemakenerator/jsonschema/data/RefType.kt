package io.github.smiley4.schemakenerator.jsonschema.data

/**
 * Types of referencing a schema
 */
enum class RefType {
    /**
     * Use the full name of a type as the reference path.
     */
    FULL,

    /**
     * Use a simple version of the name of a type as the reference path.
     * For nested classes, the name includes the names of the outer classes.
     */
    SIMPLE,

    /**
     * Use a simple version of the name of a type as the reference path.
     * For nested classes, the name does NOT include the names of the outer classes.
     */
    MINIMAL
}
