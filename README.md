# Schema-Kenerator

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator)
[![Checks Passing](https://github.com/SMILEY4/schema-kenerator/actions/workflows/checks.yml/badge.svg?branch=develop)](https://github.com/SMILEY4/schema-kenerator/actions/workflows/checks.yml)


Customizable kotlin library to extract information from classes using reflection or kotlinx-serialization and generate schemas (json-schema, swagger) from the resulting information.



## Features

- extract information from classes using reflection or kotlinx-serialization

- supports nested types, inheritance, generics, annotations 

- generate json-schema or swagger-schema

- add metadata using annotations (subtypes, title, description, deprecation, ...)

- supports jackson-annotations

- customizable and extendable



## Documentation

A wiki with a short documentation is available [here](https://github.com/SMILEY4/schema-kenerator/wiki).



## Installation

```kotlin
dependencies {
    implementation "io.github.smiley4:schema-kenerator:<VERSION>"
}
```



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
```



## Example (swagger & kotlinx-serialization)

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
