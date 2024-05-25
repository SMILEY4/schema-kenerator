# Schema-Kenerator Jackson

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-jackson/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-jackson)

This project provides common steps to support [Jackson](https://github.com/FasterXML/jackson-annotations)-annotations.

## Supported Jackson Annotations

| Step                    | Description                                                           | Relevant Steps                              |
|-------------------------|-----------------------------------------------------------------------|---------------------------------------------|
| `@JsonSubTypes`         | Includes specified subtypes for data extraction and schema generation | `collectJacksonSubTypes()`                  |
| `@JsonIgnore`           | Ignores annotated properties                                          | `handleJacksonIgnoreAnnotation()`           |
| `@JsonIgnoreType`       | Ignores properties of annotated types                                 | `handleJacksonIgnoreTypeAnnotatin()`        |
| `@JsonIgnoreProperties` | Ignores specified properties of annotated type                        | `handleJacksonIgnorePropertiesAnnotation()` |
| `@JsonProperty`         | Add support specifying name of annotated property and `required`-flag | `handleJacksonPropertyAnnotation()`         |

## Steps

| Step                                        | Description                                                                                                                                                                                                                                                                                                                                                                                                     |
|---------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `collectJacksonSubTypes()`                  | Finds and adds additional subtypes from `@JsonSubTypes`-annotations. This steps also requires the core `connectSubTypes`-step to properly connect added types.<br/>**Options:**<br/> - `typeProcessing`: turn a given type into type data containing its annotation. Usually using the `processReflection`-step again<br/> - `maxRecursionDepth`: how many nested types deep to look for "SubType"-annotations. |
| `handleJacksonIgnoreAnnotation()`           | Adds support for `@JsonIgnore`-annotation and removes annotated members from types                                                                                                                                                                                                                                                                                                                              |
| `handleJacksonIgnoreTypeAnnotatin()`        | Adds support for `@JsonIgnoreType`-annotation and removes members of the annotated type.                                                                                                                                                                                                                                                                                                                        |
| `handleJacksonIgnorePropertiesAnnotation()` | Adds support for jackson `@JsonIgnoreProperties`-annotation and removes specified members from the annotated types.                                                                                                                                                                                                                                                                                             |
| `handleJacksonPropertyAnnotation()`         | Adds support for the jackson `@JsonProperty`-annotation. Renames annotated members and modifies their nullability according to the specified values.                                                                                                                                                                                                                                                            |
