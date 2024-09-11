package io.github.smiley4.schemakenerator.swagger.data

/**
 * Types of a schema title
 */
enum class TitleType {
    /**
     * Use the full name of a type as the title.
     * Example: my.path.MyType<my.path.MyParam1,my.path.MyParam2>
     */
    FULL,
    /**
     * Use a simple version of the name of a type as the title.
     * Example: MyType<MyParam1,MyParam2>
     */
    SIMPLE,
    /**
     * Use the full name of a type as the title.
     * Example: my.path.MyType_my.path.MyParam1-my.path.MyParam2
     */
    OPENAPI_FULL,
    /**
     * Use a simple version of the name of a type as the title.
     * Example: MyType_MyParam1-MyParam2
     */
    OPENAPI_SIMPLE
}
