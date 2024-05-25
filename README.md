# Schema-Kenerator

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-core)
[![Checks Passing](https://github.com/SMILEY4/schema-kenerator/actions/workflows/checks.yml/badge.svg?branch=develop)](https://github.com/SMILEY4/schema-kenerator/actions/workflows/checks.yml)


Kotlin library to extract information from classes using reflection or kotlinx-serialization and generate schemas like json-schema or swagger-schemas from the resulting information.


## Features

- extract information from classes using reflection or kotlinx-serialization

- supports nested types, inheritance, generics, annotations 

- generate json-schema or [swagger](https://github.com/swagger-api/swagger-parser)-schema

- add metadata using additional annotations (subtypes, title, description, deprecation, ...)

- supports jackson-annotations

- customizable and extendable


## Installation

```kotlin
dependencies {
    implementation("io.github.smiley4:schema-kenerator-core:<VERSION>")
    // only for using reflection
    implementation("io.github.smiley4:schema-kenerator-reflection:<VERSION>")
    // only for using kotlinx-serialization
    implementation("io.github.smiley4:schema-kenerator-serialization:<VERSION>")
    // only for generating json-schemas
    implementation("io.github.smiley4:schema-kenerator-jsonschema:<VERSION>")
    // only for generating swagger-schemas
    implementation("io.github.smiley4:schema-kenerator-swagger:<VERSION>")
    // only for support of Jackson-annotations
    implementation("io.github.smiley4:schema-kenerator-jackson:<VERSION>")
}
```


## Documentation

A wiki with a short documentation is available [here](https://github.com/SMILEY4/schema-kenerator/wiki).


## Concept

Data extraction and schema generation happens in several steps that can be grouped into the following phases:

1. Information extraction
   1. collect relevant types that need to be analyzed
   2. process types, i.e. extract information from each type and nested types 
   3. enrich and modify extracted type data, e.g. with annotations
2. Schema generation
   1. generate individual schema for each type (and nested type)
   2. enrich and modify generated schemas, e.g. using annotations
   3. compile individual schemas into one final schema, either inlining all types or properly referencing them

Schema-Kenerator provides independent steps for each phase that can be chained together to achieve the desired result. 


## Example (json-schema & reflection)

```kotlin
class MyExampleClass(
    val someText: String,
    val someNullableInt: Int?,
    val someBoolList: List<Boolean>,
)
```

```kotlin
val jsonSchema = typeOf<MyExampleClass>()
    .processReflection()
    .generateJsonSchema()
    .withAutoTitle(TitleType.SIMPLE)
    .compileInlining()
    .json
    .prettyPrint()
```

```json
{
   "type": "object",
   "title": "MyExampleClass",
   "required": [ "someBoolList", "someText" ],
   "properties": {
     "someBoolList": {
       "type": "array",
       "title": "List<Boolean>",
       "items": {
         "type": "boolean",
         "title": "Boolean"
       }
     },
     "someNullableInt": {
       "type": "integer",
       "title": "Int",
       "minimum": -2147483648,
       "maximum": 2147483647
     },
     "someText": {
       "type": "string",
       "title": "String"
     }
   }
}
```



## Example (swagger & kotlinx-serialization)

```kotlin
dependencies {
    implementation("io.github.smiley4:schema-kenerator-core:<VERSION>")
    implementation("io.github.smiley4:schema-kenerator-serialization:<VERSION>")
    implementation("io.github.smiley4:schema-kenerator-swagger:<VERSION>")
}
```

```kotlin
@Serializable
class MyExampleClass(
    val someText: String,
    val someNullableInt: Int?,
    val someBoolList: List<Boolean>,
)
```

```kotlin
val swaggerSchema: Schema<*> = typeOf<ClassWithSimpleFields>()
    .processKotlinxSerialization()
    .generateSwaggerSchema()
    .withAutoTitle(TitleType.SIMPLE)
    .compileInlining()
    .swagger
```

```json
{
  "title" : "MyExampleClass",
  "required" : [ "someBoolList", "someText" ],
  "type" : "object",
  "properties" : {
    "someText" : {
      "title" : "String",
      "type" : "string",
      "exampleSetFlag" : false
    },
    "someNullableInt" : {
      "title" : "Int",
      "type" : "integer",
      "format" : "int32",
      "exampleSetFlag" : false
    },
    "someBoolList" : {
      "title" : "ArrayList<Boolean>",
      "type" : "array",
      "exampleSetFlag" : false,
      "items" : {
        "title" : "Boolean",
        "type" : "boolean",
        "exampleSetFlag" : false
      }
    }
  },
  "exampleSetFlag" : false
}
```
