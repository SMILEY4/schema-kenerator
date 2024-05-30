# Schema-Kenerator JSON-Schema

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-jsonschema/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-jsonschema)

This project provides steps and annotations to create JSON-Schema from type data and to customize the generated schema.

```kotlin
dependencies {
    implementation "io.github.smiley4:schema-kenerator-jsonschema:<VERSION>"
}
```

## Annotations

| Annotation      | Description                                                                                                                                                                   | Relevant Steps                 |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------|
| `@JsonTypeHint` | Specifies the json document type of the annotated class, e.g. the schema of a class annotated with `@JsonTypeHint("number")` will have the type "number" instead of "object"` | `handleJsonSchemaAnnotations` |

## Steps

| Step                            | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
|---------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `generateJsonSchema()`          | Generate an (independent) json-schema for each provided type.                                                                                                                                                                                                                                                                                                                                                                                                               |
| `withAutoTitle()`               | Adds an automatically determined `title`-property to the json-schemas. The title can either be the qualified or simple name of a type.                                                                                                                                                                                                                                                                                                                                      |
| `handleCoreAnnotations()`       | Adds<br/> - `title`-property with the value specified by a core `@Title`-annotation<br/> - `description`-property with the value specified by a core `@Description`-annotation.<br/> - `deperecated`-property with the value specified by a core `@Deprecated` or kotlin's `@Deprecated`-annotation.<br/> - `default`-property with the value specified by a core `@Default`-annotation.<br/> - `example`-properties with values specified by a core `@Example`-annotation. |
| `handleJsonSchemaAnnotations()` | Changes the `type`-property of a schema-document according to a `@JsonTypeHint`-annotation.                                                                                                                                                                                                                                                                                                                                                                                 |
| `compileInlining()`             | Creates the final JSON-Schema by inlining all additional schemas referenced by the root-schema.                                                                                                                                                                                                                                                                                                                                                                             |
| `compileReferencing()`          | Creates the final JSON-Schema by properly referencing all schemas and keeping additional schemas in the "external definitions". Primitive or simple types will usually be inlined.                                                                                                                                                                                                                                                                                          |
| `compileReferencingRoot()`      | Creates the final JSON-Schema by properly referencing all schemas and keeping all schemas - including the root schema - in "external definitions". A new root schema is created that only references the real "root" schema in the definitions. Primitive or simple types will usually be inlined.                                                                                                                                                                          |

