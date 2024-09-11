package io.github.smiley4.schemakenerator.swagger.data

/**
 * Types of referencing a schema
 */
enum class RefType {
    /**
     * Use the full name of a type as the reference path.
     * Example: my.path.MyType<my.path.MyParam1,my.path.MyParam2>
     */
    FULL,
    /**
     * Use a simple version of the name of a type as the reference path.
     * Example: MyType<MyParam1,MyParam2>
     */
    SIMPLE,
    /**
     * Use the full name of a type as the reference path.
     * Example: my.path.MyType_my.path.MyParam1-my.path.MyParam2
     */
    OPENAPI_FULL,
    /**
     * Use a simple version of the name of a type as the reference path.
     * Example: MyType_MyParam1-MyParam2
     */
    OPENAPI_SIMPLE
}
