# Schema-Kenerator Jackson Swagger

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-jackson-swagger/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-jackson-swagger)

This project provides steps to support [Jackson](https://github.com/FasterXML/jackson-annotations)-annotations specific to [Swagger](https://github.com/swagger-api/swagger-parser)-schema generation.

```kotlin
dependencies {
    implementation "io.github.smiley4:schema-kenerator-jackson-swagger:<VERSION>"
}
```

## Supported Jackson Annotations

| Step                       | Description                              | Relevant Steps                                 |
|----------------------------|------------------------------------------|------------------------------------------------|
| `@JsonPropertyDescription` | Adds description to annotated properties | `handleJacksonPropertyDescriptionAnnotation()` |

## Steps

| Step                                           | Description                                                                                      |
|------------------------------------------------|--------------------------------------------------------------------------------------------------|
| `handleJacksonPropertyDescriptionAnnotation()` | Adds a description to properties according to the jackson `@JsonPropertyDescription`-annotation. |
