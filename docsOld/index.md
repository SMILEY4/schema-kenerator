---
hide:
  - title
  - navigation
  - toc
  - footer
---

# Schema Kenerator

The schema-kenerator project consists of multiple artifacts that are used together with the goal to extract information from types and generate different schemas.
It is designed as a pipeline of individual steps to be highly configurable and flexible to match any situation.

## Features

- Analyze Java and Kotlin types using reflection or [Kotlinx.Serialization](https://github.com/Kotlin/kotlinx.serialization)
    - complex class configurations
    - recursion
    - collections, maps, enums
    - inheritance
    - type parameters
    - annotations
    - nullability and default values
    - inline types
    - ...
- Enhance types with additional information
    - [Jackson](https://github.com/FasterXML/jackson) annotations
    - [Swagger](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations) annotations
    - Javax/Jakarta validation annotations
    - ...
- Generate schemas
    - [JSON schema](https://json-schema.org/)
    - [Swagger schema](https://swagger.io/docs/specification/v3_0/data-models/data-models/)
- Highly configurable and customizable schema generation pipeline by adding new processing steps and creating own modules


## Installation

For installation instructions, see the [modules](modules.md) overview page.


## Example

```kotlin
class MyExampleClass(
    val someText: String,
    val someNullableInt: Int?,
    val someBoolList: List<Boolean>,
)
```
```kotlin
val jsonSchema = initial<String>()
    .analyzeTypeUsingReflection() //(1)!
    .generateJsonSchema() //(2)!
    .withTitle(TitleType.SIMPLE) //(3)!
    .compileInlining() //(4)!
```

1. Analyze the type using reflection and extract information
2. Generate (independent) json schemas for each associated type (here: `MyExampleClass`, `Int`, `Boolean` and `List<Boolean>`)
3. Add the simple/short name of the type as the title to the schema
4. Combine the individual schemas into a single schema for `MyExampleClass` by inlining all referenced types.

```json
{
  "title": "MyExampleClass",
  "type": "object",
  "required": ["someBoolList", "someText"],
  "properties": {
    "someBoolList": {
      "title": "List<Boolean>",
      "type": "array",
      "items": {
        "title": "Boolean",
        "type": "boolean"
      }
    },
    "someNullableInt": {
      "title": "Int",
      "type": "integer",
      "minimum": -2147483648,
      "maximum": 2147483647
    },
    "someText": {
      "title": "String",
      "type": "string"
    }
  }
}
```