# Concept

The generation of a schema from a kotlin type is done as a sequence of steps. Each step transforms or adds to the data
from the preview step. Additional steps can simply be slotted in via extension functions on the data.

The complete process can be split into two rough phases:

**1. Type Analysis**

Type analysis is the first step to generating schemas. It looks at all involved Kotlin or Java types and extracts information about methods, fields, subtypes, supertypes, type parameters and more.
This can either be done by inspecting classes using reflection or by working with the information provided by Kotlinx.Serialization

Resulting data can be modified or added to with additional steps, e.g. add missing supertype-connections or adding
additional subtypes from annotations.

Individual steps can often times also be configured, e.g. configuration of the `analyseTypeUsingReflection` specifying
whether to also include getters and other functions.

???+ example "Type Analysis"

    ```kotlin
    class MyExampleClass(
        val someText: String,
        val someNullableInt: Int?,
        val someBoolList: List<Boolean>,
    )

    typeOf<MyExampleClass>()
        .analyseTypeUsingReflection() //(1)!
        .addDiscriminatorProperty("_type") //(2)!
        //...
    ```

    1. Step analyzing the kotlin type using reflection and extracts structured information
    2. Adds a new discriminator property to differentiate between subtypes

??? info "Learn More"

    [:octicons-arrow-right-24: Analyze Types Using Reflection](analyzing_types_reflection.md)

    [:octicons-arrow-right-24: Analyze Types Using Kotlinx.Serialization](analyzing_types_reflection.md)

    [:octicons-arrow-right-24: Customization Options](customization.md)


**2. Schema Generation**

Takes the result from the type data analysis and converts it into any schema. This can be done in multiple steps to
allow for easier modification of the schema during generation, for example adding a title or descriptions.

???+ example "Schema Generation"

    ```kotlin
    class MyExampleClass(
        val someText: String,
        val someNullableInt: Int?,
        val someBoolList: List<Boolean>,
    )

    typeOf<MyExampleClass>()
        //...
        .generateJsonSchema() //(1)!
        .withTitle(TitleType.SIMPLE) //(2)!
        .compileInlining() //(3)!
    ```

    1. Generate (independent) json schemas for each associated type (here: `MyExampleClass`, `Int`, `Boolean` and `List<Boolean>`)
    2. Add the simple/short name of the type as the title to the schema
    3. Combine the individual schemas into a single schema for `MyExampleClass` by inlining all referenced types.

??? info "Learn More"

    [:octicons-arrow-right-24: Generating JSON Schema](generating_json_schema.md)

    [:octicons-arrow-right-24: Generating Swagger Schema](generating_swagger_schema.md)

    [:octicons-arrow-right-24: Customization Options](customization.md)
