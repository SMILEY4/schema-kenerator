---
hide:
  - title
  - navigation
  - toc
  - footer
---

# Schema Kenerator

Schema-Kenerator is a Kotlin project that generates schemas from Kotlin classes and types. It is designed as a pipeline of individual steps to be highly configurable and flexible to match any situation.

## Features

- **Flexible Pipeline** - Chain together individual steps to create custom schema generation workflows.
- Analyze types using **JVM Reflection** or **kotlinx.serialization**.
- Generate **JSON Schemas** or **OpenAPI Swagger Schemas**.
- Enhance schemas with **annotations** from this project or from third-party libraries like **Jackson**, **Swagger** and **Jakarta/Javax Validation**.
- **Customizable** - Create custom processing steps, type analyzers and schema transformations.
- **Comprehensive Type Support**:
    - Basic types (primitives, strings, etc.)
    - Collections (List, Set, Map, Array)
    - Nullable types
    - Enums
    - Sealed classes and interfaces
    - Generics and type parameters
    - Inheritance hierarchies
    - Recursive types
    - Inline value classes
    - Annotations

## Example

```kotlin
class MyExampleClass(
    val someText: String,
    val someNullableInt: Int?,
    val someBoolList: List<Boolean>,
)
```

Define the pipeline and generate a JSON Schema for `MyExampleClass`:

```kotlin
val jsonSchema = initial<MyExampleClass>()
    .analyzeTypeUsingReflection() 
    .generateJsonSchema() 
    .withTitle(TitleType.SIMPLE) 
    .compileInlining()
```

Resulting JSON schema:

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