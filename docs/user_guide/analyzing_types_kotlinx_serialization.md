# Analyzing Types (Kotlinx.Serialization)

Type analysis is the first step to generating a schema. The step looks at all involved Kotlin or Java types and extracts information about methods, fields, subtypes, supertypes, type parameters and more.

??? tip "Using Reflection"

    [:octicons-arrow-right-24: Information](analyzing_types_reflection.md) about type analysis using reflection


## Basics

```kotlin
@Serializable
class ExampleClass(
    val text: String,
    val number: Int?
)
```

With the `analyzeTypeUsingKotlinxSerialization()`-step, information about a given input type is extracted and resulting
information about the type as well as other referenced types, e.g. "ExampleClass", "String" and "Int" is returned to be used as inputs
for further steps.

The step can be configured to specify which information should be included in the output and in what format.

```kotlin
typeOf<ExampleClass>().analyzeTypeUsingKotlinxSerialization {
    //...
}
```

??? warning "Type Parameters"

    Kotlinx.Serialization does not provide enough information to reason about type parameters forcing the analysis step treat classes more carefully to avoid collisions which may lead to unwanted side effects during schema generation.</br>
    This more careful behaviour can be disabled for specific types that are known to never have any type parameters with the "markNotParameterized" configuration option.</br>
    This configuration may not be required for most situations, but can be used to manually resolve issues.


    ```kotlin
    typeOf<ExampleClass>().analyzeTypeUsingKotlinxSerialization {
        markNotParameterized<SimpleClass>()
        markNotParameterized(typeOf<SimpleClass>())
        markNotParameterized("example.types.MySimpleClass")
    }
    ```

??? info "Configuration Options"

    [:octicons-arrow-right-24: API Reference](../dokka/schema-kenerator-serialization/schema-kenerator-serialization/io.github.smiley4.schemakenerator.serialization/-kotlinx-serialization-type-processing-config/index.html)

??? info "Required Dependencies"

    === "Gradle (Kotlin)"
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-serialization:$version")
        ```
    
    === "Gradle"
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-serialization:$version'
        ```
    
    === "Maven"
        ```xml
        <dependency>
            <groupId>io.github.smiley4</groupId>
            <artifactId>schema-kenerator-serialization</artifactId>
            <version>${version}</version>
        </dependency>
        ```

## Inheritance and Subtypes

Subtypes of sealed classes and interfaces are detected and included automatically.

```kotlin
sealed class SealedParent { //(1)!
    class ChildOne : SealedParent()
    class ChildTwo : SealedParent()
}
```

1. Sealed class `SealedParent` with two subtypes `ChildOne` and `ChildTwo`.

```kotlin
typeOf<SealedParent>().analyzeTypeUsingKotlinxSerialization()
```

??? info "Required Dependencies"

    === "Gradle (Kotlin)"
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-serialization:$version")
        ```
    
    === "Gradle"
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-serialization:$version'
        ```
    
    === "Maven"
        ```xml
        <dependency>
            <groupId>io.github.smiley4</groupId>
            <artifactId>schema-kenerator-serialization</artifactId>
            <version>${version}</version>
        </dependency>
        ```


## Discriminator Properties

A discriminator is used to differentiate between subtypes when (de-)serializing types, i.e. to know which subtype we are dealing with.
This property is often times not included as a field in the class itself, but configured e.g. via an annotation.
This property can also be added to the type information, e.g. to be later included in generated schemas.

### Default Property

```kotlin
sealed class SealedParent { //(1)!
    class ChildOne : SealedParent()
    class ChildTwo : SealedParent()
}
```

1. Class `SealedParent` with two subtypes `ChildOne` and `ChildTwo` to differentiate between.

```kotlin
typeOf<SealedParent>()
    .analyzeTypeUsingKotlinxSerialization()
    .addDiscriminatorProperty("_type") //(1)!
```

1. Makes sure a property `_type` exists for all types that have subtypes.

An "annotation" with name `discriminator_marker` is added to the property to mark it and make it possible to find it later, e.g. when generating schemas.</br>
If a property with the name already exists, the maker annotation is added to this property instead.</br>
If a (different) property with marked with the annotation is already present, the step will NOT add the new specified property.

??? info "Required Dependencies"

    === "Gradle (Kotlin)"
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-core:$version")
        ```
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-serialization:$version")
        ```
    
    === "Gradle"
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-core:$version'
        ```
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-serialization:$version'
        ```
    
    === "Maven"
        ```xml
        <dependency>
            <groupId>io.github.smiley4</groupId>
            <artifactId>schema-kenerator-core</artifactId>
            <version>${version}</version>
        </dependency>
        ```
        ```xml
        <dependency>
            <groupId>io.github.smiley4</groupId>
            <artifactId>schema-kenerator-serialization</artifactId>
            <version>${version}</version>
        </dependency>
        ```

### Kotlinx.Serialization @JsonClassDiscriminator-annotation

A discriminator property with a name specified with the `@JsonClassDiscriminator`-annotation can be added to all types that have known subtypes. 

```kotlin
@Serializable
@JsonClassDiscriminator("_type") //(1)!
sealed class ParentDiscriminatorKotlinx { //(2)!
    @Serializable class ChildOne : SealedParent()
    @Serializable class ChildTwo : SealedParent()
}
```

1. `JsonClassDiscriminator` annotation defining the name of the discriminator property.
2. Class `ParentDiscriminatorKotlinx` with two subtypes `ChildOne` and `ChildTwo` to differentiate between.

```kotlin
ypeOf<ParentDiscriminatorKotlinx>()
    .analyzeTypeUsingKotlinxSerialization()
    .addJsonClassDiscriminatorProperty() //(1)!
```

1. Adds a property with the name specified in `@JsonClassDiscriminator` to all annotated types with subtypes.

??? info "Required Dependencies"

    === "Gradle (Kotlin)"
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-serialization:$version")
        ```
    
    === "Gradle"
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-serialization:$version'
        ```
    
    === "Maven"
        ```xml
        <dependency>
            <groupId>io.github.smiley4</groupId>
            <artifactId>schema-kenerator-serialization</artifactId>
            <version>${version}</version>
        </dependency>
        ```