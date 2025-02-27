# Concept

The generation of a schema from a kotlin type is done as a sequence of steps. Each step transforms or adds to the data
from the preview step. Additional steps can simply be slotted in via extension functions on the data.

The complete process can be split into four rough phases:

![Concept Overview](../assets/concept_light.png#only-light)
![Concept Overview](../assets/concept_dark.png#only-dark)


## 1. Initial Type Collection

Before any type can be analyzed and schema be generated, the initial types have to be provided or collected. For the most
part, this  involves providing only the one initial type to analyze and generate a schema for. However, sometimes additional
types (e.g. loosely connected subtypes) have to be added manually or collected via a step for them to be included in the final schema.

???+ example "Collecting Subtypes"

    ```kotlin
    @SubType(MySubClass::class)
    open class MyExampleClass(
        val someText: String,
        val someNullableInt: Int?,
        val someBoolList: List<Boolean>
    )

    initial<MyExampleClass>() //(1)!
        .collectSubTypes() //(2)!
        //...
    ```

    1. Starting with a single initial type to analyze.
    2. Find additional subtypes specified by a `@SubType` annotations on `MyExampleClass`



## 2. Type Analysis

Type analysis is the second step to generating schemas. It looks at all involved Kotlin or Java types and extracts information
about methods, fields, subtypes, supertypes, type parameters and more. This can either be done by inspecting classes using
reflection or by working with the information provided by Kotlinx.Serialization.

Resulting data can be further modified or added to with additional steps, e.g. add missing supertype-connections.

???+ example "Type Analysis"

    ```kotlin
    class MyExampleClass(
        val someText: String,
        val someNullableInt: Int?,
        val someBoolList: List<Boolean>,
    )

    initial<MyExampleClass>()
        .analyzeTypeUsingReflection() //(1)!
        .addDiscriminatorProperty("_type") //(2)!
        //...
    ```

    1. Step analyzing the kotlin type using reflection and extracts structured information
    2. Adds a new discriminator property to differentiate between subtypes

??? info "Learn More"

    [:octicons-arrow-right-24: Analyze Types Using Reflection](analyzing_types_reflection.md)

    [:octicons-arrow-right-24: Analyze Types Using Kotlinx.Serialization](analyzing_types_reflection.md)

    [:octicons-arrow-right-24: Customization Options](customization.md)


## 3. Schema Generation

Takes the result from the type data analysis and converts it into any schema. This can be done in multiple steps to
allow for easier modification of the schema during generation, for example adding a title or descriptions.

The result of this phase is an independent schema for each associated type. Schemas may not be completely valid at this
point, as e.g. references are not finalized yet.

???+ example "Schema Generation"

    ```kotlin
    class MyExampleClass(
        val someText: String,
        val someNullableInt: Int?,
        val someBoolList: List<Boolean>,
    )

    initial<MyExampleClass>()
        //...
        .generateJsonSchema() //(1)!
        .withTitle(TitleType.SIMPLE) //(2)!
    ```

    1. Generate (independent) json schemas for each associated type (here: `MyExampleClass`, `Int`, `Boolean` and `List<Boolean>`)
    2. Add the simple/short name of the type as the title to the schema

??? info "Learn More"

    [:octicons-arrow-right-24: Generating JSON Schema](generating_json_schema.md)

    [:octicons-arrow-right-24: Generating Swagger Schema](generating_swagger_schema.md)

    [:octicons-arrow-right-24: Customization Options](customization.md)


## 4. Schema Compilation

The final step in the pipeline is the schema compilation. This takes the previously generated and independent schemas and
merges them into the final schema.

This merging can either mean inlining all individual schemas into a single schema or keeping them separate and only referencing them.

???+ example "Schema Compilation"

    ```kotlin
    class MyExampleClass(
        val someText: String,
        val someNullableInt: Int?,
        val someBoolList: List<Boolean>,
    )

    initial<MyExampleClass>()
        //...
        .compileInlining() //(1)!
    ```

    1. Combine the individual schemas into a single schema for `MyExampleClass` by inlining all referenced types.

??? info "Learn More"

    [:octicons-arrow-right-24: Generating JSON Schema](generating_json_schema.md)

    [:octicons-arrow-right-24: Generating Swagger Schema](generating_swagger_schema.md)