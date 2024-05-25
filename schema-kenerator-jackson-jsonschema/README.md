# Schema-Kenerator Jackson JSON-Schema

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-core)

This project provides steps to support [Jackson](https://github.com/FasterXML/jackson-annotations)-annotations specific
to JSON-schema generation.

## Supported Jackson Annotations

| Step                       | Description                              | Relevant Steps                                 |
|----------------------------|------------------------------------------|------------------------------------------------|
| `@JsonPropertyDescription` | Adds description to annotated properties | `handleJacksonPropertyDescriptionAnnotation()` |

## Steps

| Step                                           | Description                                                                                      |
|------------------------------------------------|--------------------------------------------------------------------------------------------------|
| `handleJacksonPropertyDescriptionAnnotation()` | Adds a description to properties according to the jackson `@JsonPropertyDescription`-annotation. |
