@file:Suppress("ClassName")
@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.examples

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.ObjectTypeData
import io.github.smiley4.schemakenerator.core.data.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.core.renameProperties
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.customizeProperties
import io.github.smiley4.schemakenerator.jsonschema.customizeTypes
import io.github.smiley4.schemakenerator.jsonschema.data.JsonTypeHint
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.handleJsonSchemaAnnotations
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.jsonschema.withTitle
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.serialization.renameProperties
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.customizeProperties
import io.github.smiley4.schemakenerator.swagger.customizeTypes
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.kotest.core.spec.style.FreeSpec
import io.swagger.util.Json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonNamingStrategy
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.typeOf

class E04_Customization : FreeSpec({

    "custom type processing / information extraction..." - {

        // Custom type extraction functionality can be defined for specific types for both reflection and kotlinx-serialization.
        // When encountering a type (at any nesting level) that has a custom processor, the output of the custom functionality will be used instead of the default.

        @Serializable
        class ClassWithLocalDateTime(
            @Serializable(with = LocalDateTimeSerializer::class)
            val dateTime: LocalDateTime
        )

        // Here, "ClassWithLocalDateTime" contains a field of type "LocalDateTime". When generating the final json-schema,
        // "LocalDateTime" should be generated as a simple json-type "date". To achieve that, a custom processor for type "LocalDateTime"
        // is registered that returns a primitive type annotated with a "JsonTypeHint"-annotation that tells a later step (i.e. "handleJsonSchemaAnnotations")
        // to set the type of this json-object to "date"

        "... using reflection" {

            val jsonSchema = typeOf<ClassWithLocalDateTime>()
                .processReflection {

                    // register a custom processor for the type "LocalDateTime"
                    customProcessor<LocalDateTime> {
                        // Create a primitive type data for "LocalDateTime".
                        // By default, local date time would have been processed possibly as a complex object with unwanted properties.
                        PrimitiveTypeData(
                            id = TypeId.build(LocalDateTime::class.qualifiedName!!),
                            simpleName = LocalDateTime::class.simpleName!!,
                            qualifiedName = LocalDateTime::class.qualifiedName!!,
                            annotations = mutableListOf(
                                AnnotationData( // add the "JsonTypeHint" to tell the "handleJsonSchemaAnnotations"-step to treat this type as the json-type "date"
                                    name = JsonTypeHint::class.qualifiedName!!,
                                    values = mutableMapOf(
                                        "type" to "date"
                                    ),
                                    annotation = null
                                )
                            )
                        )
                    }

                }
                .generateJsonSchema()
                .handleJsonSchemaAnnotations() // read the "JsonTypeHint" annotation and set the json-object type accordingly
                .withTitle(TitleType.SIMPLE)
                .compileInlining()
                .json.prettyPrint()

            println(jsonSchema)
            // {
            //    "title": "ClassWithLocalDateTime",
            //    "type": "object",
            //    "required": [ "dateTime" ],
            //    "properties": {
            //       "dateTime": {
            //          "title": "LocalDateTime",
            //          "type": "date",
            //       }
            //    },
            // }

        }

        "... using kotlinx-serialization" {

            val jsonSchema = typeOf<ClassWithLocalDateTime>()
                .processKotlinxSerialization {

                    // register a custom processor for the type "LocalDateTime"
                    customProcessor<LocalDateTime> {
                        // Create a primitive type data for "LocalDateTime".
                        // By default, local date time would have been processed possibly as a complex object with unwanted properties.
                        PrimitiveTypeData(
                            id = TypeId.build(LocalDateTime::class.qualifiedName!!),
                            simpleName = LocalDateTime::class.simpleName!!,
                            qualifiedName = LocalDateTime::class.qualifiedName!!,
                            annotations = mutableListOf(
                                AnnotationData( // add the "JsonTypeHint" to tell the "handleJsonSchemaAnnotations"-step to treat this type as the json-type "date"
                                    name = JsonTypeHint::class.qualifiedName!!,
                                    values = mutableMapOf(
                                        "type" to "date"
                                    ),
                                    annotation = null
                                )
                            )
                        )
                    }

                }
                .generateJsonSchema()
                .handleJsonSchemaAnnotations() // read the "JsonTypeHint" annotation and set the json-object type accordingly
                .withTitle(TitleType.SIMPLE)
                .compileInlining()
                .json.prettyPrint()

            println(jsonSchema)
            // {
            //    "title": "ClassWithLocalDateTime",
            //    "type": "object",
            //    "required": [ "dateTime" ],
            //    "properties": {
            //       "dateTime": {
            //          "title": "LocalDateTime",
            //          "type": "date",
            //       }
            //    },
            // }
        }

    }

    "type redirection" - {

        // Similar to registering custom processing logic for specific types, "type redirects" can be registered at the type processing step
        // for both reflection and kotlinx-serialization. When encountering the specified type (at any nesting level), instead of processing
        // and extracting data from the actual type, it will be replaced with the other provided type and this one will be processed instead.

        @Serializable
        class ClassWithLocalDateTime(
            @Serializable(with = LocalDateTimeSerializer::class)
            val dateTime: LocalDateTime
        )

        "reflection" {

            val jsonSchema = typeOf<ClassWithLocalDateTime>()
                .processReflection {
                    // redirect the type "LocalDateTime" to "String". Everytime the type "LocalDateTime" is encountered, it will be replaced with "String".
                    redirect<LocalDateTime, String>()
                }
                .generateJsonSchema()
                .withTitle(TitleType.SIMPLE)
                .compileInlining()
                .json.prettyPrint()

            println(jsonSchema)
            // {
            //    "title": "ClassWithLocalDateTime",
            //    "type": "object",
            //    "required": [ "dateTime" ],
            //    "properties": {
            //       "dateTime": {
            //          "title": "String",
            //          "type": "string"
            //       }
            //    }
            // }

        }

        "kotlinx-serialization" {

            val jsonSchema = typeOf<ClassWithLocalDateTime>()
                .processKotlinxSerialization {
                    // redirect the type "LocalDateTime" to "String". Everytime the type "LocalDateTime" is encountered, it will be replaced with "String".
                    redirect<LocalDateTime, String>()
                }
                .generateJsonSchema()
                .withTitle(TitleType.SIMPLE)
                .compileInlining()
                .json.prettyPrint()

            println(jsonSchema)
            // {
            //    "title": "ClassWithLocalDateTime",
            //    "type": "object",
            //    "required": [ "dateTime" ],
            //    "properties": {
            //       "dateTime": {
            //          "title": "String",
            //          "type": "string"
            //       }
            //    }
            // }

        }

    }

    "renaming properties" - {

        // All properties of all types can be renamed using the "renameProperties()"-step.

        class ExampleClass(
            val securePassword: String,
            val usernameOrEmail: String,
        )

        "manual renaming" {

            // The renaming can be done manually by taking the original name of each property and outputting a new name,
            // e.g. here by adding a prefix to all properties.

            val jsonSchema = typeOf<ExampleClass>()
                .processReflection()
                .renameProperties { originalName -> "prefix_$originalName" }
                .generateJsonSchema()
                .compileInlining()
                .json.prettyPrint()

            println(jsonSchema)
            // {
            //    "type": "object",
            //    "required": [ "prefix_securePassword", "prefix_usernameOrEmail" ],
            //    "properties": {
            //       "prefix_securePassword": {
            //          "type": "string"
            //       },
            //       "prefix_usernameOrEmail": {
            //          "type": "string"
            //       }
            //    }
            // }

        }

        "kotlinx-serialization naming strategy" {

            // Properties can also be renamed by providing a "JsonNamingStrategy" from kotlinx-serialization.
            // Note: schema-kenerator-serialization is required, even though this also works without using kotlinx-serialization for data extraction.

            val jsonSchema = typeOf<ExampleClass>()
                .processReflection()
                .renameProperties(JsonNamingStrategy.SnakeCase)
                .generateJsonSchema()
                .compileInlining()
                .json.prettyPrint()

            println(jsonSchema)
            // {
            //    "type": "object",
            //    "required": [ "secure_password", "username_or_email" ],
            //    "properties": {
            //       "secure_password": {
            //          "type": "string"
            //       },
            //       "username_or_email": {
            //          "type": "string"
            //       }
            //    }
            // }

        }

    }

    "manually modifying types and properties" - {

        // If functionality or modifications are required that is not covered by any of the existing steps, custom steps can be added to the sequence.
        // For "simple" operations, i.e. modifying generated json-schema or swagger-schema types and properties, two utility steps can be used:
        // customizeTypes:      is called for every individual type with the original extracted type data and the current json/swagger schema as inputs
        // customizeProperties: is called for every individual property of each type with the original extracted property data and the current json/swagger schema as inputs

        class ExampleClass(
            val someProperty: String,
            val secretValue: String
        )

        // This example
        // - checks each type if it contains a property with the substring "secret" in the name. If it finds one it adds a note in the description of the type.
        // - checks each property if its name contains the substring "secret" - it yes, it adds a note in the description of the property.

        "json-schema" {

            val jsonSchema = typeOf<ExampleClass>()
                .processReflection()
                .generateJsonSchema()
                .customizeTypes { typeData, typeSchema ->
                    if (typeData is ObjectTypeData && typeData.members.any { it.name.contains("secret") } && typeSchema is JsonObject) {
                        typeSchema.properties["description"] = JsonTextValue("Note: A secret property has been detected!")
                    }
                }
                .customizeProperties { propertyData, propertySchema ->
                    if(propertyData.name.contains("secret") && propertySchema is JsonObject) {
                        propertySchema.properties["description"] = JsonTextValue("Note: This property was detected as a secret property!")
                    }
                }
                .compileInlining()
                .json.prettyPrint()

            println(jsonSchema)
            // {
            //    "type": "object",
            //    "description": "Note: A secret property has been detected!",
            //    "required": [
            //       "secretValue",
            //       "someProperty"
            //    ],
            //    "properties": {
            //       "secretValue": {
            //          "type": "string",
            //          "description": "Note: This property was detected as a secret property!"
            //       },
            //       "someProperty": {
            //          "type": "string"
            //       }
            //    }
            // }
        }

        "swagger" {

            val swaggerSchema = typeOf<ExampleClass>()
                .processReflection()
                .generateSwaggerSchema()
                .customizeTypes { typeData, typeSchema ->
                    if (typeData is ObjectTypeData && typeData.members.any { it.name.contains("secret") }) {
                        typeSchema.description = "Note: A secret property has been detected!"
                    }
                }
                .customizeProperties { propertyData, propertySchema ->
                    if(propertyData.name.contains("secret")) {
                        propertySchema.description = "Note: This property was detected as a secret property!"
                    }
                }
                .compileInlining()
                .swagger

            println(Json.prettyPrint(swaggerSchema))
            // {
            //   "type" : "object",
            //   "description" : "Note: A secret property has been detected!",
            //   "required" : [ "secretValue", "someProperty" ],
            //   "properties" : {
            //     "secretValue" : {
            //       "type" : "string",
            //       "description" : "Note: This property was detected as a secret property!",
            //     },
            //     "someProperty" : {
            //       "type" : "string",
            //     }
            //   }
            // }

        }

    }

}) {
    companion object {

        @OptIn(ExperimentalSerializationApi::class)
        @Serializer(forClass = LocalDateTime::class)
        object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
            private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

            override fun serialize(encoder: Encoder, value: LocalDateTime) {
                encoder.encodeString(value.format(formatter))
            }

            override fun deserialize(decoder: Decoder): LocalDateTime {
                return LocalDateTime.parse(decoder.decodeString(), formatter)
            }
        }

    }
}
