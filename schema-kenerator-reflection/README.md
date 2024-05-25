# Schema-Kenerator Reflection

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-serialization/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-serialization)

This project provides steps and annotations to extract and modify information from kotlin types using reflection.

```kotlin
dependencies {
    implementation "io.github.smiley4:schema-kenerator-reflection:<VERSION>"
}
```

## Annotations

| Annotation | Description                                                                                                                                  | Relevant Steps    |
|------------|----------------------------------------------------------------------------------------------------------------------------------------------|-------------------|
| `@SubType` | Explicitly specifies one or more direct subtypes of the annotated type. Subtypes are otherwise only detected automatically for sealed types. | `collectSubTypes` |

## Steps

| Step                  | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `collectSubTypes()`   | Collects (recursively) all additional types specified by the `@SubType`-annotation to include them in the process/extraction step.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `processReflection()` | Analyze the types and extract information.<br/>**Options:** <br/> - `includeGetters`: whether to include getters as members. Getters are functions that take no parameter, return a value and begin with "get" or "is".<br/> - `includeWeakGetters`: whether to include weak getters as members. Weak getters are functions that take no parameter, return a value but do not begin with "get" or "is".<br/> - `includeFunctions`: Whether to include normal functions as members of classes<br/> - `includeHidden`: Whether to include "hidden", e.g. private properties as members of classes<br/> - `includeStatic`: Whether to include static properties as members of classes<br/> - `primitiveTypes`: the list of kotlin types that are considered "primitive"<br/> - `enumConstType`: Whether to use the "name" or "toString" for the values of enum constants<br/> - *custom processors*: overwrite the default behavior for specific types and implement custom information extractor |

