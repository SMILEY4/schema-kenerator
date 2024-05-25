# Schema-Kenerator Core

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-core)

This project provides the base data models as well as some common steps and annotations.

## Annotations

| Annotation           | Description                                                                    | Relevant Steps                                      |
|----------------------|--------------------------------------------------------------------------------|-----------------------------------------------------|
| `@SchemaName`        | Specify a new name for the type.                                               | `renameTypes` (core)                                |
| `@SchemaTitle`       | Provide an additional title for the resulting schema.                          | `handleTitleAnnotation` (jsonschema, swagger)       |
| `@SchemaDescription` | Provide a description for the annotated type or property.                      | `handleDescriptionAnnotation` (jsonschema, swagger) |
| `@SchemaDeprecated`  | Specify whether the annotated type or property should be marked as deprecated. | `handleDeprecatedAnnotation` (jsonschema, swagger)  |
| `@SchemaExample`     | Provide example values for the annotated type or property                      | `handleExampleAnnotation` (jsonschema, swagger)     |
| `@SchemaDefault`     | Specify a default value for the annotated type or property                     | `handleDefaultAnnotation` (jsonschema, swagger)     |

## Steps

| Step                | Description                                                                                                                                                                                                                   |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `connectSubTypes()` | Adds missing subtype-supertype-relations between processed types. Types not already present in the input are not included. This step is mainly used to find and fix missing connections between types added from other steps. |
| `renameTypes()`     | Renames types according to provided `@SchemaName`-annotations.                                                                                                                                                                |
| `mergeGetters()`    | Removes getter-functions and merges them with their matching properties or creates new properties when no matching one could be found.                                                                                        |
