# Customization

## Type Redirection

Type redirection allows for specific types to be analyzed and included in schemas instead of the actual types.
This can be used to customize or annotate classes from external libraries.

"Type redirects" can be registered at the type analyzing step for both reflection and kotlinx-serialization.
When encountering the specified type (at any nesting level), instead of analyzing and extracting data from the actual
type, it will be replaced with the other provided type and this one will be analyzed instead.

???+ example "Handling `LocalDateTime` as `String`"

    === "Reflection"

        ```kotlin
        class ExampleClass(
            val dateTime: LocalDateTime
        )
        ```
        ```kotlin
        initial<ExampleClass>()
            .analyzeTypeUsingReflection {
                redirect {
                    from<LocalDateTime>(TypeRedirect.FromNullability.IGNORE) //(1)!
                    to<String>(TypeRedirect.ToNullability.KEEP) //(2)!
                }
            }
            .generateJsonSchema()
            .compileInlining()
        ```
        
        1. Replace the type `LocalDateTime` while ignoring nullability, i.e. replace every instance of `LocalDateTime` and `LocalDateTime?`.
        2. Replace `LocalDateTime` with type `String` while keeping the nullability of the original property / type.
        
        ```json
        {
           "type": "object",
           "required": [ "dateTime" ],
           "properties": {
              "dateTime": {
                 "type": "string"
              }
           }
        }
        ```

    === "Kotlinx.Serialization"

        ```kotlin
        @Serializable
        class ExampleClass(
            @Serializable(with = LocalDateTimeSerializer::class)
            val dateTime: LocalDateTime
        )
        ```
        ```kotlin
        initial<ExampleClass>()
            .analyzeTypeUsingKotlinxSerialization {
                redirect {
                    from<LocalDateTime>(TypeRedirect.FromNullability.IGNORE) //(1)!
                    to<String>(TypeRedirect.ToNullability.KEEP)  //(2)!
                }
            }
            .generateJsonSchema()
            .compileInlining()
        ```
        
        1. Replace the type `LocalDateTime` while ignoring nullability, i.e. replace every instance of `LocalDateTime` and `LocalDateTime?`.
        2. Replace `LocalDateTime` with type `String` while keeping the nullability of the original property / type.
        
        ```json
        {
           "title": "ExampleClass",
           "type": "object",
           "required": [ "dateTime" ],
           "properties": {
              "dateTime": {
                 "title": "String",
                 "type": "string"
              }
           }
        }
        ```

**Handling nullability while matching types to replace** 

Whether the nullability of a type should be considered while matching types to replace can be defined using `TypeRedirect.FromNullability`.

- `TypeRedirect.FromNullability.IGNORE` - Ignore nullability of types when matching, e.g. when redirecting from type `Int`, all instances of `Int` and `Int?` get replaced by the specified new type.
- `TypeRedirect.FromNullability.MATCH` - Match nullability exactly, e.g. when redirecting from type `Int?`, only instances of `Int?` get replaced and those of type `Int` stay untouched.

**Handling nullability when replacing**

Whether the type after replacing should keep the original nullability or if it should be replaced as well can be specified using `TypeRedirect.ToNullability`.

- `TypeRedirect.ToNullability.KEEP` - keep the original nullability of the member, e.g. when redirecting from `LocalDateTime` to `String`, a nullable field of `LocalDateTime?` will stay nullable and become `String?`. 
- `TypeRedirect.ToNullability.REPLACE` - replace the original nullability of the member with the nullability of the specified "to" type, e.g. when redirecting from `OptionalInt` to `Int?`, all non-nullable fields `OptionalInt` will become nullable `Int?`.


## Custom Type Analyzer

Custom type analysis functionality can be defined for specific types with both reflection and Kotlinx.Serialization.
When encountering a type (at any nesting level) that has a custom analyzer registered, the output of the custom functionality will be used instead of the default result.
This can be used to overwrite the output of specific types with custom information or own behavior.

???+ Example "Custom Type Data for `LocalDateTime`"

    `ExampleClass` contains a field of type `LocalDateTime`. When generating the json-schema, `LocalDateTime` should be
    generated as a simple object with type "string" and format "date-time". To achieve that, a custom analyzer is registered for type `LocalDateTime`
    that returns data for a string type annotated with a `@Format` annotation that tells a later step (i.e. `handleCoreAnnotations`)
    to set the format of this json-object to "date-time".
    
    === "Reflection"
    
        ```kotlin
        class ExampleClass(
            val dateTime: LocalDateTime
        )
        ```
        ```kotlin
        initial<ExampleClass>()
            .analyzeTypeUsingReflection {
                custom<LocalDataTime> { //(1)!
                    TypeData(
                        id = TypeId.create(), //(2)!
                        identifyingName = TypeName("kotlin.String", "String"), //(3)!
                        descriptiveName = TypeName("java.time.LocalDateTime", "LocalDateTime"), //(4)!
                        annotations = mutableListOf(
                            AnnotationData(
                                name = "io.github.smiley4.schemakenerator.core.annotations.Format", //(5)!
                                values = mutableMapOf(
                                    "type" to "date-time" //(6)!
                                )
                            )
                        )
                    )
                }
            }
            .generateJsonSchema()
            .withTitle(TitleType.SIMPLE) //(7)!
            .handleCoreAnnotations() //(8)!
            .compileInlining()
        ```

        1. Register custom handling for `LocalDateTime`. Types can also be registered using `LocalDateTime::class` or the qualified name of the class.
        2. Create a new id for `LocalDateTime`. Types are identified by their id, not by their name.
        3. The full and short name identifying this type. When generating a schema, this name will ususally be used to determine the type of the json or swagger schema object.
        4. The "actual" full and short name of the type. This name is usually used for titles, reference paths, etc.
        5. Add an `@Format` annotation on the type. This annotation will be handled in the `handleCoreAnnotations`-step to add the `format` property to the schema.
        6. Set the attributes of the annotation. Here `type` to `date-time`.
        7. Add the descriptive names of the types as titles to the schema. 
        8. Handle (among others) `@Format` annotations to set the format property in the schemas.

        ```json
        {
           "title": "ClassWithLocalDateTime",
           "type": "object",
           "required": [ "dateTime" ],
           "properties": {
              "dateTime": {
                 "title": "LocalDateTime",
                 "type": "string",
                 "format": "date-time"
              }
           }
        }
        ```

    === "Kotlinx.Serialization"
    
        ```kotlin
        @Serializable
        class ExampleClass(
            @Serializable(with = LocalDateTimeSerializer::class)
            val dateTime: LocalDateTime
        )
        ```
        ```kotlin
        initial<ExampleClass>()
            .analyzeTypeUsingKotlinxSerialization {
                custom<LocalDataTime> { //(1)!
                    TypeData(
                        id = TypeId.create(), //(2)!
                        identifyingName = TypeName("kotlin.String", "String"), //(3)!
                        descriptiveName = TypeName("java.time.LocalDateTime", "LocalDateTime"), //(4)!
                        annotations = mutableListOf(
                            AnnotationData(
                                name = "io.github.smiley4.schemakenerator.core.annotations.Format", //(5)!
                                values = mutableMapOf(
                                    "type" to "date-time" //(6)!
                                )
                            )
                        )
                    )
                }
            }
            .generateJsonSchema()
            .withTitle(TitleType.SIMPLE) //(7)!
            .handleCoreAnnotations() //(8)!
            .compileInlining()
        ```

        1. Register custom handling for `LocalDateTime`. Types can also be registered using `LocalDateTime::class` or the qualified name of the class.</br> When using custom serializers, the name must match the name specified by the serializer.
        2. Create a new id for `LocalDateTime`. Types are identified by their id, not by their name.
        3. The full and short name identifying this type. When generating a schema, this name will ususally be used to determine the type of the json or swagger schema object.
        4. The "actual" full and short name of the type. This name is usually used for titles, reference paths, etc.
        5. Add an `@Format` annotation on the type. This annotation will be handled in the `handleCoreAnnotations`-step to add the `format` property to the schema.
        6. Set the attributes of the annotation. Here `type` to `date-time`.
        7. Add the descriptive names of the types as titles to the schema. 
        8. Handle (among others) the `@Format` annotations to set the format property in the schemas.

        ```json
        {
           "title": "ClassWithLocalDateTime",
           "type": "object",
           "required": [ "dateTime" ],
           "properties": {
              "dateTime": {
                 "title": "LocalDateTime",
                 "type": "string",
                 "format": "date-time"
              }
           }
        }
        ```

## Modifying Generated Schemas

Schema-Kenerator provides utility steps to more easily modify schemas after they have been generated.

- The action of the `customizeTypes`-step is called for every schema generated from a type.
- The action of the `customizeProperties`-step is called for every property of every generated schema.

???+ example "Customizing Generated JSON Schemas and Properties"

    ```kotlin
    initial<ExampleClass>()
        .analyzeTypeUsingReflection()
        .generateJsonSchema()
        .customizeTypes { typeData, typeSchema -> //(1)!
            if (typeData.members.any { it.name.contains("secret") }) { //(2)!
                typeSchema.properties["description"]
                    = JsonTextValue("Note: A secret property has been detected!") //(3)!
            }
        }
        .customizeProperties { propertyData, propertySchema -> //(4)!
            if(propertyData.name.contains("secret")) { //(5)!
                propertySchema.properties["description"]
                    = JsonTextValue("Note: This property was detected as a secret property!") //(6)!
            }
        }
        .compileInlining()
    ```
    
    1. Action is called for every schema generated from a type. The schema for the type as well as the type data from the type analysis is provided to the action.
    2. Check if the type contains any member with a name containing "secret".
    3. Add a "description" to the schema of the type.
    4. Action is called for every property of every generated schema. The schema for the property as well as the member type data from the type analysis is provided to the action.
    5. Check if the name of the property contains the string "secret".
    6. Add a "description" to the schema of the property.