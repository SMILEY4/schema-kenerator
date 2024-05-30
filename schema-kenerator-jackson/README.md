# Schema-Kenerator Jackson

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-jackson/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-jackson)

This project provides common steps to support [Jackson](https://github.com/FasterXML/jackson-annotations)-annotations.

```kotlin
dependencies {
    implementation "io.github.smiley4:schema-kenerator-jackson:<VERSION>"
}
```

## Supported Jackson Annotations

| Step                    | Description                                                           | Relevant Steps               |
|-------------------------|-----------------------------------------------------------------------|------------------------------|
| `@JsonSubTypes`         | Includes specified subtypes for data extraction and schema generation | `collectJacksonSubTypes()`   |
| `@JsonIgnore`           | Ignores annotated properties                                          | `handleJacksonAnnotations()` |
| `@JsonIgnoreType`       | Ignores properties of annotated types                                 | `handleJacksonAnnotations()` |
| `@JsonIgnoreProperties` | Ignores specified properties of annotated type                        | `handleJacksonAnnotations()` |
| `@JsonProperty`         | Add support specifying name of annotated property and `required`-flag | `handleJacksonAnnotations()` |

## Steps

| Step                         | Description                                                                                                                                                                                                                                                                                                                                                                                                     |
|------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `collectJacksonSubTypes()`   | Finds and adds additional subtypes from `@JsonSubTypes`-annotations. This steps also requires the core `connectSubTypes`-step to properly connect added types.<br/>**Options:**<br/> - `typeProcessing`: turn a given type into type data containing its annotation. Usually using the `processReflection`-step again<br/> - `maxRecursionDepth`: how many nested types deep to look for "SubType"-annotations. |
| `handleJacksonAnnotations()` | Adds support for<br/> - `@JsonIgnore`-annotation and removes annotated members from types<br/> - `@JsonIgnoreType`-annotation and removes members of the annotated type.<br/> - `@JsonIgnoreProperties`-annotation and removes specified members from the annotated types.<br/> - `@JsonProperty`-annotation. Renames annotated members and modifies their nullability according to the specified values.       |
