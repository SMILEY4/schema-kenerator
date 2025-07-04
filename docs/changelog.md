---
search:
  exclude: true
---

# Changelog

## 2.2.0

**Improvements to JSON schema generation:**

- explicit nullables handling now configurable (now on parity with swagger schema). `nullables` property in `generateJsonSchema`-step.
- renamed `optionalHandling` property to `optionals` in `generateJsonSchema`-step (now on parity with swagger schema).
- added `mergePropertyAttributesIntoType`-step (now on parity with swagger schema)
- changed default reference path from #/definitions/MySchema to #/$defs/MySchema to better match jsonschema spec (path is configurable at compile step)
- added new step `merge` that creates a single json schema (from the output of the compile steps) with all separate referenced schemas placed in a `$defs` section (configurable) of the root schema.

**Improvements to Swagger schema generation:**

- added previously missing support for `@Swagger#requiredMode` property
- improved handling of schemas with multiple types (e.g. due to `@Type`-annotation) in specific situations. This change prevents results like `"type": ["object", "myType"]` where the type "object" should have been replaced by "myType".

**Other improvements and fixes:**

- kotlinx-serialization can now distinguish between lists (with any items) and sets (with unique items) and set the flag accordingly (now on parity with reflection type analysis)
- improved check determining whether a schema should be inlined or referenced. This should lead to simpler and more predictable schemas.

## 2.1.4

Upgrade dependency versions
- Jackson 2.15.3 -> 2.19.1
- Jackson Serialization Json 1.7.3 -> 1.8.1
- Swagger Parser 2.1.24 -> 2.1.30
- Jakarta Validation 3.0.0 -> 3.1.1
- Kotest 5.8.0 -> 5.9.1
- forced commons-codec 1.11 -> 1.13 (transitive dependency)

## 2.1.3

- improved handling of recursion with inlined schemas: if the root type of a schema is used recursively, it is referenced instead of inlined.
- fixed: null pointer exception when using multiple `@Contextual` annotations
- fixed: fixed handling of `@Description` annotation when added to properties and classes

## 2.1.2

- fixed bug: duplicate "string" type added by type discriminator handling
- fixed bug: incorrect comparison check in "mergePropertyAttributesIntoType" step caused infinite loop

## 2.1.1

- fix infinite loop in `mergePropertyAttributesIntoType`-step ([#50](https://github.com/SMILEY4/schema-kenerator/issues/50), [#51](https://github.com/SMILEY4/schema-kenerator/issues/51))

## 2.1.0

- add initial step extension functions to all InitialTypeData instead of specific subclasses

## 2.0.0

- re-structured reflection and kotlinx-serialization type analysis
    - allow more powerful custom type analyzers
    - open up more internals for custom processors while still being able to re-use common functions
- re-structures swagger and json schema generation
    - allow more powerful custom schema generators
- reworked/replaced bundles & overhauled input data classes for steps to make flow of data and order of steps more clear
    - generic bundles replaced with specific classes "InitialTypeData", "TypeDataGroup", "IntermediateXYSchemaData", "CompiledXYSchemaData"
    - start of pipeline changed from extension functions on raw kotlin types (e.g. typeOf<T>, T::class, etc.) to initial<T>, initial(T::clas), etc.
- overhauled type redirection
    - enhance configuration dsl
    - more options to define exact behavior
        - matching
            - MATCH: whether the nullability of the "from" has to match exactly
            - IGNORE: nullability of the "from" type is ignored when matching redirects
        - replacing
            - KEEP: whether the nullability of the "from" type should be kept with the replaced type
            - REPLACE: whether the nullability is also overwritten
- reworked base data models to better fit requirements and make them more flexible
- added option to enable/disable explicit "null"-types for properties (e.g. `.compileInlining(explicitNullTypes = true)`
- added option to allow special float values (i.e. "NaN", "Infinity", "-Infinity") for Float and Double (e.g. `.generateSwaggerSchema { allowSpecialFloatingPointValues = true }`)
- added option to generate schemas for maps with complex keys as array with the map key and map values as valid item types (e.g. `.generateSwaggerSchema { mapsWithStructuredKeysAsArrays = true }`)
- added option to swagger schema generation to restrict values of discriminator properties to the name of the type (`strictDiscriminatorProperty`))
- option to handle nullable properties as required or non-required (e.g. `.generateSwaggerSchema { nullables = RequiredHandling.REQUIRED }`)
- added support for kotlinx-serialization `@Contextual` annotation and `@Serializable(with = ...)`
- improved support for annotations on fields that modify the type/schema (e.g. `@Format` on properties)
- renamed steps to better reflect their function and purpose:
    - `connectSubTypes` to `addMissingSupertypeSubtypeRelations`
    - `mergeGetters` to `gettersToProperties`
    - `renameProperties` to `renameMembers`
    - `processReflection` to `analyzeTypeUsingReflection`
    - `processSerialization` to `analyzeTypeUsingKotlinxSerialization`
- removed deprecated "withAutoTitle" step
- shortened & simplified package structure
- make classes "internal" to reduce namespace pollution
- simplified and removed unnecessary dependencies, opened up transitive dependencies to library consumers
- fixed bug: type parameter nullability information was lost
- fixed bug: `@Type`-annotation not handled correctly
- fixed renaming swagger schema properties [#44](https://github.com/SMILEY4/schema-kenerator/issues/44)

## 1.6.4

- fixed bug: when using reflection to analyze generic types, too many interfaces might be detected as subtypes [#43](https://github.com/SMILEY4/schema-kenerator/issues/43)
- fixed bug: when generating schema for a type overwriting a property with a more specific type, the wrong type was used in the resulting schema [#43](https://github.com/SMILEY4/schema-kenerator/issues/43)

## 1.6.3

- fixed: unchanged properties of swagger `@Schema` and `@ArraySchema` annotations were not ignored during schema generation

## 1.5.1

- fixed: unchanged properties of swagger `@Schema` and `@ArraySchema` annotations were not ignored during schema generation

## 1.6.2

- fixed/add support for top-level generics with kotlinx

## 1.6.1

- upgrade schema `io.swagger.parser.v3:swagger-parser` from 2.1.20 to 2.1.24

## 1.6.0

- added core annotations to specify schema 'type' and 'format': `@Schema`, `@Type`. Can be added on classes and fields
- exposes swagger library via gradle "api"
- swagger compilation step treats both openapi30 `type` and openapi31 `types` as valid types
- fixed bug: NPE during swagger compile inline step when using classes that have a nullable sealed class property

## 1.5.0

- added support for discriminator mapping in swagger schemas
- added support for example property on swagger `@Schema`-annotation [#32](https://github.com/SMILEY4/schema-kenerator/pull/32)
- added new core annotation `@Format` [#36](https://github.com/SMILEY4/schema-kenerator/pull/36)
- upgrade kotlin to 2.0.21
- fixed: naming conflict in core `@Default` annotation [#35](https://github.com/SMILEY4/schema-kenerator/pull/35)

## 1.4.3

- fixed: swagger enum types were missing type "string"

## 1.4.2

- fixed bug: swagger schema now internally uses `types` (for openapi 3.1) instead of `type` (for openapi 3.0) for everything [#28](https://github.com/SMILEY4/schema-kenerator/issues/28)
- fixed bug: ignore default/unchanged values in swagger "Schema"-annotation [#31](https://github.com/SMILEY4/schema-kenerator/pull/31)
- fixed bug: properly merge annotations on properties with annotations on types [#27](https://github.com/SMILEY4/schema-kenerator/issues/27)
- fixed bug: null-pointer exception [#26](https://github.com/SMILEY4/schema-kenerator/pull/26)
- fixed bug: (json-schema) properties with same types share modifications even though they should be applied to specific properties

## 1.4.1

- fix bug: wrong formatting with swagger TitleType/PathType "OPENAPI_FULL" and "OPENAPI_SIMPLE"

## 1.4.0

- added step `renameProperties` to rename properties (including support for kotlinx-serialization JsonNamingStrategy)
- support for discriminators
  - `addDiscriminatorProperty`-step to add discriminator property with specified name to all types with subtypes
  - `addJacksonTypeInfoDiscriminatorProperty`-step to add discriminator property with name specified by jackson JsonTypeInfo-annotation to all types with subtypes
  - `addJsonClassDiscriminatorProperty`-step to add discriminator property with name specified by kotlinx-serialization JsonClassDiscriminator-annotation to all types with subtypes
  - discriminator property is included in swagger schema-generation
- add project schema-kenerator-validations with support for javax and jackarta validation annotation in swagger-schemas
  - supported jakarta annotations:
    - NotNull
    - NotEmpty
    - NotBlank
    - Size
    - Min
    - Max
  - supported javax annotations:
    - NotNull
    - NotEmpty
    - NotBlank
    - Size
    - Min
    - Max

- annotations on constructor parameters are added to annotations of matching property
- handle nullability of fields according to openapi 3.1.0, e.g. nullable string as `type: ["string", "null"]`
- fixed bug: required-annotation is ignored when all fields of a class would be nullable/optional
- fixed bug: nullpointer-exception (see issue #22)

## 1.3.0

please ignore this version - i messed up during release here

## 1.2.3

- fix issues with nullability of fields

## 1.2.2

- fixed bug: wrong format for openapi TitleType and RefType

## 1.2.1

- fixed bug: wrong format for TitleType.OPENAPI_SIMPLE

## 1.2.0

- added `TitleType.OPENAPI_SIMPLE`, `TitleType.OPENAPI_FULL`, `RefType.OPENAPI_SIMPLE`, `RefType.OPENAPI_FULL`  as options for swagger that generate results confirming to the openapi-spec
- `RefType.OPENAPI_FULL` replaces `RefType.FULL` as the default for swagger schemas
- renames step `withAutoTitle(...)` to `withTitle(...)`
- step `withTitle(...)` can take a builder function to provide a custom title
- step `compileReferencing(...)` and `compileReferencingRoot(...)` can take a builder function to provide a custom path
- fixed bug: redirecting to nullable types did not work correctly

## 1.1.1

- fixed bug: swagger example property not set correctly. Used examples instead of example.
- fixed bug: exception when using Schema-annotation with blank minimum and maximum
- fixed bug: Default-annotation not handled for properties (swagger and json-schema)

## 1.1.0

- added steps `customizeTypes` and `customizeProperties` for easier manual customization of json-schema and swagger-schema

- added support for optional properties
  - detect optional parameters (non nullable, but with a default value provided)
  - handle in schema generation step as required or non required (configurable)

- added support for inline value classes
  - detect value classes
  - inline "wrapped" type in schema generation

- added config option `knownNotParameterized` for type processing with kotlinx-serialization to manually prevent single type appearing multiple times in some setups

- added support for annotations with kotlinx-serialization

- fixed: type redirects not working with kotlinx-serialization

- fixed: exception when qualified name is missing, e.g. for local classes

## 1.0.0

- 🥳

## 0.4.0

- reworked naming of steps to be more consistent and clearer
- detect whether a collection allows only unique elements (i.e. is a set) and include uniqueItems in json and swagger schema generation
- implemented type redirects to allow processing a custom type instead of the real one
- fix: exception when filtering members of some types
- fix: exception when processing type parameters of some types

## 0.3.0

- merge annotations from kotlin property and java field to fix annotations not being able to be detected
- add support for Jackson annotations
  - `@JsonPropertyDescription`
  - `@JsonIgnore`
  - `@JsonIgnoreType`
  - `@JsonIgnoreProperties`
  - `@JsonProperty`
- add support for swagger annotations
  - `@Schema`
  - `@ArraySchema`
- add new step to merge getters with their properties

## 0.2.0

- added SchemaName annotation to "rename" types (qualified and simple name)
- added options to choose whether to use the name or the toString-function for enum constants
- fix type parameters missing for maps and collections
- fix schema reference paths
- improved dsl of custom processors


## 0.1.1

- fixed bug with typeId parser
- fixed build config

## 0.1.0

- initial version
