## Add Dependencies

Schema Kenerator is split into multiple modules. You'll need to include:

1. **Core module** (always required)
    - `schema-kenerator-core`
2. **Type analysis module** (choose one or both):
    - `schema-kenerator-reflection` - for JVM reflection
    - `schema-kenerator-serialization` - for kotlinx.serialization
3. **Schema generation module** (choose one or both):
    - `schema-kenerator-jsonschema` - for JSON Schema
    - `schema-kenerator-swagger` - for OpenAPI/Swagger
4. **Optional enhancement modules**:
    - `schema-kenerator-jackson` - Jackson annotation support
    - `schema-kenerator-jackson-jsonschema` - Jackson + JSON Schema
    - `schema-kenerator-jackson-swagger` - Jackson + Swagger
    - `schema-kenerator-validation-swagger` - Jakarta/Javax validation + Swagger

Schema Kenerator is published to Maven Central and can be added to projects using Gradle or Maven:

=== "Gradle (Kotlin DSL)"

    ```kotlin
    implementation("io.github.smiley4:schema-kenerator-core:$version")
    implementation("io.github.smiley4:schema-kenerator-reflection:$version")
    implementation("io.github.smiley4:schema-kenerator-jsonschema:$version")
    ...
    ```

=== "Gradle (Groovy)"

    ```groovy
    implementation 'io.github.smiley4:schema-kenerator-core:$version'
    implementation 'io.github.smiley4:schema-kenerator-reflection:$version'
    implementation 'io.github.smiley4:schema-kenerator-jsonschema:$version'
    ...
    ```

=== "Maven"

    ```xml
    <dependency>
        <groupId>io.github.smiley4</groupId>
        <artifactId>schema-kenerator-core</artifactId>
        <version>${version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.smiley4</groupId>
        <artifactId>schema-kenerator-reflection</artifactId>
        <version>${version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.smiley4</groupId>
        <artifactId>schema-kenerator-jsonschema</artifactId>
        <version>${version}</version>
    </dependency>
    ...
    ```

## Generating a JSON Schema with JVM Reflection

The required dependencies must be installed:

```
schema-kenerator-core
schema-kenerator-reflection
schema-kenerator-jsonschema
```

A JSON schema should be generated for the following class:

```kotlin
data class Book(
    val isbn: String,
    val title: String,
    val author: String,
    val publicationYear: Int,
    val genres: List,
    val inStock: Boolean
)
```

Generate the schema by defining a basic generation pipeline:

```kotlin
val schema = initial<Book>() // (1)!
    .analyzeTypeUsingReflection() // (2)!
    .generateJsonSchema() // (3)!
    .compileInlining() // (4)!
```

1. Provide the type to generate the schema for.
2. Analyze the type using JVM reflection and extract required information.
3. Generate an intermediate json schema for each involved type.
4. Merge all intermediate schemas into a single final schema.

Examine the produced schema:

```kotlin
println(schema.json.prettyPrint())
```

```json
{
  "type": "object",
  "required": ["isbn", "title", "author", "publicationYear", "genres", "inStock"],
  "properties": {
    "isbn": {
      "type": "string"
    },
    "title": {
      "type": "string"
    },
    "author": {
      "type": "string"
    },
    "publicationYear": {
      "type": "integer",
      "minimum": -2147483648,
      "maximum": 2147483647
    },
    "genres": {
      "type": "array",
      "items": {
        "type": "string"
      }
    },
    "inStock": {
      "type": "boolean"
    }
  }
}
```