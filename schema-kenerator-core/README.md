# Schema-Kenerator Core

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-core)

Contains the core data models as well as some common steps and annotations.

```kotlin
dependencies {
    implementation "io.github.smiley4:schema-kenerator-core:<VERSION>"
}
```

## Annotations

| Annotation     | Description                                                                    | Relevant Steps                                |
|----------------|--------------------------------------------------------------------------------|-----------------------------------------------|
| `@Name`        | Specify a new name for the type.                                               | `handleNameAnnotation` (core)                 |
| `@Title`       | Provide an additional title for the resulting schema.                          | `handleCoreAnnotations` (jsonschema, swagger) |
| `@Description` | Provide a description for the annotated type or property.                      | `handleCoreAnnotations` (jsonschema, swagger) |
| `@Deprecated`  | Specify whether the annotated type or property should be marked as deprecated. | `handleCoreAnnotations` (jsonschema, swagger) |
| `@Example`     | Provide example values for the annotated type or property                      | `handleCoreAnnotations` (jsonschema, swagger) |
| `@Default`     | Specify a default value for the annotated type or property                     | `handleCoreAnnotations` (jsonschema, swagger) |

## Steps

| Step                     | Description                                                                                                                                                                                                                   |
|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `connectSubTypes()`      | Adds missing subtype-supertype-relations between processed types. Types not already present in the input are not included. This step is mainly used to find and fix missing connections between types added from other steps. |
| `mergeGetters()`         | Removes getter-functions and merges them with their matching properties or creates new properties when no matching one could be found.                                                                                        |
| `handleNameAnnotation()` | Renames types according to provided `@Name`-annotations.                                                                                                                                                                      |
