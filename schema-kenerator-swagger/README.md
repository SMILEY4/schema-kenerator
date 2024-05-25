# Schema-Kenerator Swagger

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-swagger/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-swagger)

This project provides steps and annotations to create [Swagger](https://github.com/swagger-api/swagger-parser)-Schema
from type data and to customize the generated schema.

## Annotations

| Annotation         | Description                                                                                                                                                                       | Relevant Steps                    |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|
| `@SwaggerTypeHint` | Specifies the swagger schema type of the annotated class, e.g. the schema of a class annotated with `@SwaggerTypeHint("number")` will have the type "number" instead of "object"` | `handleSwaggerTypeHintAnnotation` |

## Supported Swagger-Annotations

| Annotation                                       | Description                                         | Relevant Step                          |
|--------------------------------------------------|-----------------------------------------------------|----------------------------------------|
| `@Schema(title=...)` on types & properties       | Sets the `title`-property of the schema.            | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(description=...)` on types & properties | Sets the `description`-property of the schema.      | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(name=...)` on properties                | Sets the `name`-property of the schema.             | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(hidden=...)` on properties              | Sets the `hidden`-property of the schema.           | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(allowableValues=...)` on properties     | Sets the `allowableValues`-property of the schema.  | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(defaultValue=...)` on properties        | Sets the `defaultValue`-property of the schema.     | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(accessMode=...)` on properties          | Sets the `accessMode`-property of the schema.       | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(minLength=...)` on properties           | Sets the `minLength`-property of the schema.        | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(maxLength=...)` on properties           | Sets the `maxLength`-property of the schema.        | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(format=...)` on properties              | Sets the `format`-property of the schema.           | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(minimum=...)` on properties             | Sets the `minimum`-property of the schema.          | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(maximum=...)` on properties             | Sets the `maximum`-property of the schema.          | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(exclusiveMaximum=...)` on properties    | Sets the `exclusiveMaximum`-property of the schema. | `handleSwaggerSchemaAnnotation()`      |
| `@Schema(exclusiveMinimum=...)` on properties    | Sets the `exclusiveMinimum`-property of the schema. | `handleSwaggerSchemaAnnotation()`      |
| `@ArraySchema(minItems=...)` on properties       | Sets the `minItems`-property of the schema.         | `handleSwaggerArraySchemaAnnotation()` |
| `@ArraySchema(maxItems=...)` on properties       | Sets the `maxItems`-property of the schema.         | `handleSwaggerArraySchemaAnnotation()` |
| `@ArraySchema(uniqueItems=...)` on properties    | Sets the `uniqueItems`-property of the schema.      | `handleSwaggerArraySchemaAnnotation()` |

## Steps

| Step                                | Description                                                                                                                                                                                                                                                                                         |
|-------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `generateSwaggerSchema()`           | Generate an (independent) swagger-schema for each provided type.                                                                                                                                                                                                                                    |
| `withAutoTitle()`                   | Adds an automatically determined `title`-property to the swagger-schemas. The title can either be the qualified or simple name of a type.                                                                                                                                                           |
| `handleTitleAnnotation()`           | Adds `title`-property with the value specified by a core `@SchemaTitle`-annotation.                                                                                                                                                                                                                 |
| `handleDescriptionAnnotation()`     | Adds `description`-property with the value specified by a core `@SchemaDescription`-annotation.                                                                                                                                                                                                     |
| `handleDeprecatedAnnotation()`      | Adds `deperecated`-property with the value specified by a core `@SchemaDeprecated` or kotlin's `@Deprecated`-annotation.                                                                                                                                                                            |
| `handleDefaultAnnotation()`         | Adds `default`-property with the value specified by a core `@SchemaDefault`-annotation.                                                                                                                                                                                                             |
| `handleExampleAnnotation()`         | Adds `example`-properties with values specified by a core `@SchemaExample`-annotation.                                                                                                                                                                                                              |
| `handleSwaggerTypeHintAnnotation()` | Changes the `type`-property of a schema according to a `@SwaggerTypeHint`-annotation.                                                                                                                                                                                                               |
| `compileInlining()`                 | Creates the final swagger-schema by inlining all additional schemas referenced by the root-schema.                                                                                                                                                                                                  |
| `compileReferencing()`              | Creates the final swagger-schema by properly referencing all schemas and keeping additional schemas in the "external components". Primitive or simple types will usually be inlined.                                                                                                                |
| `compileReferencingRoot()`          | Creates the final swagger-schema by properly referencing all schemas and keeping all schemas - including the root schema - in "external components". A new root schema is created that only references the real "root" schema in the components. Primitive or simple types will usually be inlined. |

