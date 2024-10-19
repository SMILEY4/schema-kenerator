# Schema-Kenerator Swagger

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-swagger/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-swagger)

This project provides steps and annotations to create [Swagger](https://github.com/swagger-api/swagger-parser)-Schema
from type data and to customize the generated schema.

```kotlin
dependencies {
    implementation "io.github.smiley4:schema-kenerator-swagger:<VERSION>"
}
```

## Annotations

| Annotation         | Description                                                                                                                                                                       | Relevant Steps                    |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|
| `@SwaggerTypeHint` | Specifies the swagger schema type of the annotated class, e.g. the schema of a class annotated with `@SwaggerTypeHint("number")` will have the type "number" instead of "object"` | `handleSwaggerTypeHintAnnotation` |

## Supported Swagger-Annotations

| Annotation                                       | Description                                         | Relevant Step               |
|--------------------------------------------------|-----------------------------------------------------|-----------------------------|
| `@Schema(title=...)` on types & properties       | Sets the `title`-property of the schema.            | `handleSchemaAnnotations()` |
| `@Schema(description=...)` on types & properties | Sets the `description`-property of the schema.      | `handleSchemaAnnotations()` |
| `@Schema(example=...)` on types & properties     | Sets the `example`-property of the schema.          | `handleSchemaAnnotations()` |
| `@Schema(name=...)` on properties                | Sets the `name`-property of the schema.             | `handleSchemaAnnotations()` |
| `@Schema(hidden=...)` on properties              | Sets the `hidden`-property of the schema.           | `handleSchemaAnnotations()` |
| `@Schema(allowableValues=...)` on properties     | Sets the `allowableValues`-property of the schema.  | `handleSchemaAnnotations()` |
| `@Schema(defaultValue=...)` on properties        | Sets the `defaultValue`-property of the schema.     | `handleSchemaAnnotations()` |
| `@Schema(accessMode=...)` on properties          | Sets the `accessMode`-property of the schema.       | `handleSchemaAnnotations()` |
| `@Schema(minLength=...)` on properties           | Sets the `minLength`-property of the schema.        | `handleSchemaAnnotations()` |
| `@Schema(maxLength=...)` on properties           | Sets the `maxLength`-property of the schema.        | `handleSchemaAnnotations()` |
| `@Schema(format=...)` on properties              | Sets the `format`-property of the schema.           | `handleSchemaAnnotations()` |
| `@Schema(minimum=...)` on properties             | Sets the `minimum`-property of the schema.          | `handleSchemaAnnotations()` |
| `@Schema(maximum=...)` on properties             | Sets the `maximum`-property of the schema.          | `handleSchemaAnnotations()` |
| `@Schema(exclusiveMaximum=...)` on properties    | Sets the `exclusiveMaximum`-property of the schema. | `handleSchemaAnnotations()` |
| `@Schema(exclusiveMinimum=...)` on properties    | Sets the `exclusiveMinimum`-property of the schema. | `handleSchemaAnnotations()` |
| `@ArraySchema(minItems=...)` on properties       | Sets the `minItems`-property of the schema.         | `handleSchemaAnnotations()` |
| `@ArraySchema(maxItems=...)` on properties       | Sets the `maxItems`-property of the schema.         | `handleSchemaAnnotations()` |
| `@ArraySchema(uniqueItems=...)` on properties    | Sets the `uniqueItems`-property of the schema.      | `handleSchemaAnnotations()` |

## Steps

| Step                         | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `generateSwaggerSchema()`    | Generate an (independent) swagger-schema for each provided type.<br/> - `optionalHandling`: How to handle optional properties, i.e. non-nullable properties with a provided default value.                                                                                                                                                                                                                                                                                                                 |
| `withTitle()`                | Adds an automatically determined `title`-property to the swagger-schemas. The title is created either specified by the given `TitleType` or by a provided function.                                                                                                                                                                                                                                                                                                                                        |
| `handleCoreAnnotations()`    | Adds<br/> - `title`-property with the value specified by a core `@SchemaTitle`-annotation.<br/> - `description`-property with the value specified by a core `@SchemaDescription`-annotation.<br/> - `deperecated`-property with the value specified by a core `@SchemaDeprecated` or kotlin's `@Deprecated`-annotation.<br/> - `default`-property with the value specified by a core `@SchemaDefault`-annotation.<br/> - `example`-properties with values specified by a core `@SchemaExample`-annotation. |
| `handleSwaggerAnnotations()` | Changes the `type`-property of a schema according to a `@SwaggerTypeHint`-annotation.                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `handleSchemaAnnotations()`  | Adds properties from swagger `@Schema`-annotation (see table above for supported properties)                                                                                                                                                                                                                                                                                                                                                                                                               |                                                                                                                                                                                                                                                                                                                                                                                                              |
| `customizeTypes()`           | Manually modify the generated schema for types.                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `customizeProperties()`      | Manually modify the generated schema for properties.                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| `compileInlining()`          | Creates the final swagger-schema by inlining all additional schemas referenced by the root-schema.                                                                                                                                                                                                                                                                                                                                                                                                         |
| `compileReferencing()`       | Creates the final swagger-schema by properly referencing all schemas and keeping additional schemas in the "external components". Primitive or simple types will usually be inlined.                                                                                                                                                                                                                                                                                                                       |
| `compileReferencingRoot()`   | Creates the final swagger-schema by properly referencing all schemas and keeping all schemas - including the root schema - in "external components". A new root schema is created that only references the real "root" schema in the components. Primitive or simple types will usually be inlined.                                                                                                                                                                                                        |

