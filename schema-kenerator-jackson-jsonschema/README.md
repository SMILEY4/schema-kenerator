# Schema-Kenerator Jackson JSON-Schema

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-jackson-jsonschema/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-jackson-jsonschema)

This project provides steps to support [Jackson](https://github.com/FasterXML/jackson-annotations)-annotations specific
to JSON-schema generation.

```kotlin
dependencies {
    implementation "io.github.smiley4:schema-kenerator-jackson-jsonschema:<VERSION>"
}
```

## Supported Jackson Annotations

| Step                       | Description                              | Relevant Steps                         |
|----------------------------|------------------------------------------|----------------------------------------|
| `@JsonPropertyDescription` | Adds description to annotated properties | `handleJacksonJsonSchemaAnnotations()` |

## Steps

| Step                                   | Description                                                                                      |
|----------------------------------------|--------------------------------------------------------------------------------------------------|
| `handleJacksonJsonSchemaAnnotations()` | Adds a description to properties according to the jackson `@JsonPropertyDescription`-annotation. |
