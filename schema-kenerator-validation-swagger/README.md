# Schema-Kenerator Validation Swagger

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-validation-swagger/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-validation-swagger)

This project provides steps to support [Javax](https://mvnrepository.com/artifact/javax.validation/validation-api)
and [Jakarta](https://github.com/jakartaee/validation/tree/main) validation annotations when generating
the [Swagger](https://github.com/swagger-api/swagger-parser) schema.

```kotlin
dependencies {
    implementation "io.github.smiley4:schema-kenerator-validation-swagger:<VERSION>"
}
```

## Supported Validation-Annotations

| Annotation                  | Description                                                                             |
|-----------------------------|-----------------------------------------------------------------------------------------|
| `@NotNull()` on properties  | Marks the property as a required property in the Swagger schema.                        |
| `@NotEmpty()` on properties | Marks the property as a required property in the Swagger schema.                        |
| `@NotBlank()` on properties | Marks the property as a required property in the Swagger schema.                        |
| `@Size()` on properties     | Sets the minimum and/or maximum size (or length) of the property in the Swagger schema. |
| `@Min()` on properties      | Sets the minimum numerical value of the property in the Swagger schema.                 |
| `@Max()` on properties      | Sets the maximum numerical value of the property in the Swagger schema.                 |

## Steps

| Step                                   | Description                                                             |
|----------------------------------------|-------------------------------------------------------------------------|
| `handleJavaxValidationAnnotations()`   | Check for any `javax.validation.constraints` annotations on the type.   |
| `handleJakartaValidationAnnotations()` | Check for any `jakarta.validation.constraints` annotations on the type. |
