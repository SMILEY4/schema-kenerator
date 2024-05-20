package io.github.smiley4.schemakenerator.jsonschema.data

/**
 * Types of referencing a schema
 */
enum class RefType {
    /**
     * use the full name of a type as the reference path
     */
    FULL,
    /**
     * use a simple version of the name of a type as the reference path
     */
    SIMPLE
}
