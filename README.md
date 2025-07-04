# Schema Kenerator

[![Version](https://img.shields.io/maven-central/v/io.github.smiley4/schema-kenerator-core?style=flat&color=blue&logo=apachemaven)](https://central.sonatype.com/search?q=github.smiley4.schema-kenerator-*)
[![Checks Passing](https://img.shields.io/github/actions/workflow/status/SMILEY4/schema-kenerator/checks.yml?style=flat&logo=github)](https://github.com/SMILEY4/schema-kenerator/actions/workflows/checks.yml)
[![License](https://img.shields.io/github/license/SMILEY4/schema-kenerator?style=flat&color=teal)](https://github.com/SMILEY4/schema-kenerator/blob/develop/LICENSE)


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


## Documentation

A wiki with documentation is available [here](https://smiley4.github.io/schema-kenerator/latest).

Examples showcasing and explaining the functionalities and use cases of this project can be found [here](https://github.com/SMILEY4/schema-kenerator/tree/develop/schema-kenerator-examples/src/test/kotlin/io/github/smiley4/schemakenerator/examples).


## Installation

See [modules](https://smiley4.github.io/schema-kenerator/latest/modules/) wiki page for installation instructions.


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
    // Analyze the type using reflection and extract information
    .analyzeTypeUsingReflection()
    // Generate (independent) json schemas for each associated type (here: `MyExampleClass`, `Int`, `Boolean` and `List<Boolean>`)
    .generateJsonSchema()
    // Add the simple/short name of the type as the title to the schema
    .withTitle(TitleType.SIMPLE)
    // Combine the individual schemas into a single schema for `MyExampleClass` by inlining all referenced types.
    .compileInlining()
```
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
