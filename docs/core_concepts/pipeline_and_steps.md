# Pipeline and Steps

Schema Kenerator uses a **pipeline architecture** where schema generation is broken down into individual, composable
steps.
This design provides flexibility and enables customization at every stage of the generation process.

## The Pipeline Concepts

Instead of a monolithic `generateSchema()` function, Schema Kenerator chains together steps that progressively collect,
transform and enrich data:

```kotlin
initial()             // Step 1: Start with a type
    .analyzeType()    // Step 2: Extract type information  
    .generateSchema() // Step 3: Create schema objects
    .customize()      // Step 4: Modify schemas
    .compile()        // Step 5: Produce final output
```

Each step in the pipeline accepts an input from the previous step, applies a transformation returns its output ready for
the next step. Steps can be added, omitted reordered or replaced (with constraints).

This enables developers to compose exactly the workflow required for specific use cases.

## The Four Phases of Schema Generation

Schema generation typically follows four distinct phases.While the specific steps within each phase may vary, the
overall
structure remains consistent.

### Phase 1: Type Collection

Specify which types should be included in schema generation.

```kotlin
initial()
```

The `initial()` function accepts a root type and returns a bundle containing that type.

Some scenarios require manually specifying additional types:

```kotlin
initial(
    associatedTypes = listOf(
        typeOf(),
        typeOf()
    )
)
```

This can be useful when e.g. working with non-sealed inheritance hierarchies.

These additional types can also be collected automatically:

```kotlin
TODO
```

Output is a `Bundle<KType>` containing the type(s) to analyze.

### Phase 2: Type Analysis

Extract structured information from Kotlin/Java types.

```kotlin
.analyzeTypeUsingReflection()
// or
    .analyzeTypeUsingKotlinxSerialization()
```

Type analysis inspects types and extracts information required for the next steps, like:
- fields, properties, functions, ...
- type information and type parameters
- nullability and optional values
- annotations on types and members
- inheritance relationships (supertypes and subtypes)

The analysis process is recursive - when a property references another class, that class is also analyzed.

Output is a `Bundle<TypeData>` containing structured information about all involved types:

```kotlin
data class User(
    val name: String,
    val age: Int
)

val bundle = initial().analyzeTypeUsingReflection()
// bundle.root        -> TypeData for "User"
// bundle.supporting  -> TypeData for "String" and "Int"
```

### Phase 3: Schema Generation

Convert type information into schema objects.

```kotlin
.generateJsonSchema()
// or
    .generateSwaggerSchema()
```

This phase transforms extracted type data into intermediate schemas.

At this stage, schemas are **independent** - each type has its own schema, but they're not yet properly connected or
compiled into a final structure.

Output is a `Bundle<JsonSchema>` or `Bundle<SwaggerSchema>` - one schema per type.

```kotlin
data class User(
    val name: String,
    val age: Int
)

val bundle = initial()
    .analyzeTypeUsingReflection()
    .generateJsonSchema()

// bundle.root        -> JSON Schema for "User"
// bundle.supporting  -> JSON Schemas for "String" and "Int"
```

### Phase 4: Schema Compilation

Merge independent schemas into final, valid output.

```kotlin
.compileInlining()
// or
    .compileReferencing()
// or
    .compileReferencingRoot()
```

The compilation phase resolves references between schemas and produces the final output.

**Compilation Strategies:**

| Strategy             | Description                                                 | Use Case                             |
|----------------------|-------------------------------------------------------------|--------------------------------------|
| **Inlining**         | Embeds all referenced types into a single schema            | Simple schemas, single output schema |
| **Referencing**      | Uses `$ref` to reference common schema definitions          | Complex schemas, reusable components |
| **Referencing Root** | Like referencing, but also moves root schema to definitions | Maximum reusability                  |

Outputs final schema object(s) ready for use.