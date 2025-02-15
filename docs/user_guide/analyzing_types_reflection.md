# Analyzing Types (Reflection)

Type analysis is the first step to generating a schema. The step looks at all involved Kotlin or Java types and extracts information about methods, fields, subtypes, supertypes, type parameters and more.

??? tip "Using Kotlinx.Serialization"

    [:octicons-arrow-right-24: Information](analyzing_types_kotlinx_serialization.md) about type analysis using Kotlinx.Serialization


## Basics

```kotlin
class ExampleClass(
    val text: String,
    val number: Int?
)
```

With the `analyseTypeUsingReflection()`-step, information about a given input type is extracted and resulting
information about the type as well as other referenced types, e.g. "ExampleClass", "String" and "Int" is returned to be used as inputs
for further steps.

The step can be configured to specify which information should be included in the output and in what format.

```kotlin
typeOf<ExampleClass>().analyseTypeUsingReflection {
    //...
}
```

??? info "Configuration Options"

    [:octicons-arrow-right-24: API Reference](../dokka/schema-kenerator-reflection/schema-kenerator-reflection/io.github.smiley4.schemakenerator.reflection/-reflection-type-analysis-config/index.html)

??? info "Required Dependencies"

    === "Gradle (Kotlin)"
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-reflection:$version")
        ```
    
    === "Gradle"
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-reflection:$version'
        ```
    
    === "Maven"
        ```xml
        <dependency>
            <groupId>io.github.smiley4</groupId>
            <artifactId>schema-kenerator-reflection</artifactId>
            <version>${version}</version>
        </dependency>
        ```

## Inheritance and Subtypes

### Sealed Class and Interfaces

Subtypes of sealed classes and interfaces are detected and included automatically.

```kotlin
sealed class SealedParent { //(1)!
    class ChildOne : SealedParent()
    class ChildTwo : SealedParent()
}
```

1. Sealed class `SealedParent` with two subtypes `ChildOne` and `ChildTwo`.

```kotlin
typeOf<SealedParent>().analyseTypeUsingReflection()
```

??? info "Required Dependencies"

    === "Gradle (Kotlin)"
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-reflection:$version")
        ```
    
    === "Gradle"
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-reflection:$version'
        ```
    
    === "Maven"
        ```xml
        <dependency>
            <groupId>io.github.smiley4</groupId>
            <artifactId>schema-kenerator-reflection</artifactId>
            <version>${version}</version>
        </dependency>
        ```

### Manually Including Subtypes

When using classes that are not "sealed", subtypes are not automatically detected and have to be added as input types for them to be respected in the analysis.

```kotlin
open class ParentManual { //(1)!
    class ChildOne : ParentManual()
    class ChildTwo : ParentManual()
}
```

1. Class `ParentManual` with two subtypes `ChildOne` and `ChildTwo`.

```kotlin
val inputTypes = Bundle(
    data = typeOf<ParentManual>(), //(1)!
    supporting = listOf(
        typeOf<ParentManual.ChildOne>(), //(2)!
        typeOf<ParentManual.ChildTwo>()
    )
)

inputTypes
    .analyseTypeUsingReflection() //(3)!
    .addMissingSupertypeSubtypeRelations() //(4)!
```

1. The main type we want to analyze and get information for.
2. Additional types we want to include when extracting information.
3. Extract information from all input types individually, i.e. from `ParentManual`, `ChildOne`, `ChildTwo`
4. `ChildOne` and `ChildTwo` reference `ParentManual` as their supertype, but `ParentManual` does not yet know about its subtypes.</br>This step finds and fills in these missing connections, i.e. adds `ChildOne` and `ChildTwo` to the subtypes of `ParentManual`.

??? info "Required Dependencies"

    === "Gradle (Kotlin)"
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-core:$version")
        ```
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-reflection:$version")
        ```
    
    === "Gradle"
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-core:$version'
        ```
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-reflection:$version'
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
            <artifactId>schema-kenerator-reflection</artifactId>
            <version>${version}</version>
        </dependency>
        ```

### Core @Subtype-Annotation

Instead of adding the possible subtypes to the input manually, they can be connected automatically using the `schema-kenerator-core` `@SubType`-annotation.

```kotlin
@SubType(ParentCore.ChildOne::class) //(1)!
@SubType(ParentCore.ChildTwo::class)
open class ParentCore { //(2)!
    class ChildOne : ParentCore()
    class ChildTwo : ParentCore()
}
```

1. All subtypes defined on the parent class using `@SubType`-annotations.
2. Class `ParentManual` with two subtypes `ChildOne` and `ChildTwo`.


```kotlin
typeOf<ParentCore>()
    .collectSubTypes() //(1)!
    .analyseTypeUsingReflection()
    .addMissingSupertypeSubtypeRelations() //(2)!
```

1. This step looks at the `@SubType`-annotations present on any input type (recursive) and adds the referenced types to be included as inputs in the next steps.
2. `ChildOne` and `ChildTwo` reference `ParentCore` as their supertype, but `ParentCore` does not yet know about its subtypes.</br>This step finds and fills in these missing connections, i.e. adds `ChildOne` and `ChildTwo` to the subtypes of `ParentCore`.

??? info "Required Dependencies"

    === "Gradle (Kotlin)"
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-core:$version")
        ```
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-reflection:$version")
        ```
    
    === "Gradle"
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-core:$version'
        ```
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-reflection:$version'
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
            <artifactId>schema-kenerator-reflection</artifactId>
            <version>${version}</version>
        </dependency>
        ```

### Jackson @JsonSubTypes-Annotation

Instead of adding the possible subtypes to the input manually, they can also be connected automatically using the [Jackson](https://github.com/FasterXML/jackson) `@JsonSubTypes`-annotation.

```kotlin
@JsonSubTypes(
    JsonSubTypes.Type(value = ParentJackson.ChildOne::class), //(1)!
    JsonSubTypes.Type(value = ParentJackson.ChildTwo::class),
)
open class ParentJackson { //(2)!
    class ChildOne : ParentJackson()
    class ChildTwo : ParentJackson()
}
```

1. All subtypes defined on the parent class using `@JsonSubTypes`-annotation.
2. Class `ParentJackson` with two subtypes `ChildOne` and `ChildTwo`.


```kotlin
typeOf<ParentJackson>()
    .collectJacksonSubTypes({ //(1)!
        it.analyseTypeUsingReflection() //(2)!
    })
    .analyseTypeUsingReflection()
    .addMissingSupertypeSubtypeRelations() //(3)!
```

1. The "collectJacksonSubTypes()"-step looks at the @JsonSubTypes-annotation present on any type (recursive) and adds the referenced types to be included in the next steps.
2. this step needs to intermediate information from the types. This specifies how this information is extracted. reflection is recommended here.
3. `ChildOne` and `ChildTwo` reference `ParentJackson` as their supertype, but `ParentJackson` does not yet know about its subtypes.</br>This step finds and fills in these missing connections, i.e. adds `ChildOne` and `ChildTwo` to the subtypes of `ParentJackson`.

??? info "Required Dependencies"

    === "Gradle (Kotlin)"
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-reflection:$version")
        ```
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-jackson:$version")
        ```
    
    === "Gradle"
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-reflection:$version'
        ```
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-jackson:$version'
        ```
    
    === "Maven"
        ```xml
        <dependency>
            <groupId>io.github.smiley4</groupId>
            <artifactId>schema-kenerator-reflection</artifactId>
            <version>${version}</version>
        </dependency>
        ```
        ```xml
        <dependency>
            <groupId>io.github.smiley4</groupId>
            <artifactId>schema-kenerator-jackson</artifactId>
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
    .analyseTypeUsingReflection()
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
        implementation("io.github.smiley4:schema-kenerator-reflection:$version")
        ```
    
    === "Gradle"
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-core:$version'
        ```
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-reflection:$version'
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
            <artifactId>schema-kenerator-reflection</artifactId>
            <version>${version}</version>
        </dependency>
        ```

### Jackson @JsonTypeInfo-annotation

A discriminator property with a name specified with the [Jackson](https://github.com/FasterXML/jackson) `@JsonClassDiscriminator`-annotation can be added to all types that have known subtypes.
Only @JsonTypeInfo#include "PROPERTY" and "EXISTING_PROPERTY" is supported.

```kotlin
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY, //(1)!
    property = "_type" //(2)!
)
sealed class ParentDiscriminatorJackson { //(3)!
    class ChildOne : SealedParent()
    class ChildTwo : SealedParent()
}
```

1. Specify how to include the (new) property. Only `PROPERTY` and `EXISTING_PROPERTY` are currently supported.
2. Specify the name of the (new) property.
3. Class `ParentDiscriminatorJackson` with two subtypes `ChildOne` and `ChildTwo` to differentiate between.

```kotlin
typeOf<ParentDiscriminatorJackson>()
    .analyseTypeUsingReflection()
    .addJacksonTypeInfoDiscriminatorProperty() //(1)!
```

1.  Adds a property with the name specified in `@JsonTypeInfo#property` to all annotated types with subtypes.

An "annotation" with name `discriminator_marker` is added to the property to mark it and make it possible to find it later, e.g. when generating schemas.</br>
If a property with the name already exists, the maker annotation is added to this property instead.</br>
If a (different) property with marked with the annotation is already present, the step will NOT add the new specified property.


??? info "Required Dependencies"

    === "Gradle (Kotlin)"
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-reflection:$version")
        ```
        ```kotlin
        implementation("io.github.smiley4:schema-kenerator-jackson:$version")
        ```
    
    === "Gradle"
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-reflection:$version'
        ```
        ```groovy
        implementation 'io.github.smiley4:schema-kenerator-jackson:$version'
        ```
    
    === "Maven"
        ```xml
        <dependency>
            <groupId>io.github.smiley4</groupId>
            <artifactId>schema-kenerator-reflection</artifactId>
            <version>${version}</version>
        </dependency>
        ```
        ```xml
        <dependency>
            <groupId>io.github.smiley4</groupId>
            <artifactId>schema-kenerator-jackson</artifactId>
            <version>${version}</version>
        </dependency>
        ```