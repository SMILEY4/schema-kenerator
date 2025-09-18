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
     * For nested classes, the name includes the names of the outer classes.
     * Example: MyType<MyParam1,MyParam2>
     */
    SIMPLE,

    /**
     * Use a simple version of the name of a type as the reference path.
     * For nested classes, the name does NOT include the names of the outer classes.
     * Example: MyType<MyParam1,MyParam2>
     */
    MINIMAL,

    /**
     * Use the full name of a type as the reference path.
     * Example: my.path.MyType_my.path.MyParam1-my.path.MyParam2
     */
    OPENAPI_FULL,

    /**
     * Use a simple version of the name of a type as the reference path.
     * For nested classes, the name includes the names of the outer classes.
     * Example: MyType_MyParam1-MyParam2
     */
    OPENAPI_SIMPLE,

    /**
     * Use a simple version of the name of a type as the reference path.
     * For nested classes, the name does NOT include the names of the outer classes.
     * Example: MyType_MyParam1-MyParam2
     */
    OPENAPI_MINIMAL
}
