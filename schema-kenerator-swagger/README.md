# Schema-Kenerator Swagger

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-swagger/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-swagger)

This project provides steps and annotations to create [Swagger](https://github.com/swagger-api/swagger-parser)-Schema
from type data and to customize the generated schema.

## Annotations

| Annotation         | Description                                                                                                                                                                       | Relevant Steps                    |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|
| `@SwaggerTypeHint` | Specifies the swagger schema type of the annotated class, e.g. the schema of a class annotated with `@SwaggerTypeHint("number")` will have the type "number" instead of "object"` | `handleSwaggerTypeHintAnnotation` |

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

