@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.smiley4.schemakenerator.core.annotations.Format
import io.github.smiley4.schemakenerator.core.annotations.Required
import io.github.smiley4.schemakenerator.core.annotations.Type
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.core.renameMembers
import io.github.smiley4.schemakenerator.core.typedata.TypeData
import io.github.smiley4.schemakenerator.jackson.handleJacksonAnnotations
import io.github.smiley4.schemakenerator.jsonschema.OptionalHandling
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.customizeProperties
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.handleCoreAnnotations
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.serialization.renameMembers
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.mergePropertyAttributesIntoType
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.github.smiley4.schemakenerator.validation.swagger.handleJavaxValidationAnnotations
import io.kotest.core.spec.style.FreeSpec
import io.swagger.v3.oas.models.media.Schema
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import java.time.Instant
import java.util.Optional
import java.util.UUID
import javax.validation.constraints.Size
import kotlin.reflect.typeOf

class MiscTests : FreeSpec({

    "https://github.com/SMILEY4/schema-kenerator/issues/14 - redirect to nullable types" - {

        "reflection" {
            val result = typeOf<TestClassIssue14a>()
                .processReflection {
                    redirect<Optional<String?>, String?>()
                }
                .generateJsonSchema()
                .compileInlining()

            result.json.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "required": [],
                      "properties": {
                        "name": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent()
            }
        }

        "kotlinx-serialization" {
            val result = typeOf<TestClassIssue14b>()
                .processKotlinxSerialization {
                    redirect<Int, String?>()
                }
                .generateJsonSchema()
                .compileInlining()

            result.json.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "required": [],
                      "properties": {
                        "name": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent()
            }
        }

    }

    "https://github.com/SMILEY4/schema-kenerator/issues/16 - field nullability handling" - {

        "reflection" {
            val result = typeOf<TestClassIssue16>()
                .processReflection()
                .generateJsonSchema {
                    optionalHandling = OptionalHandling.NON_REQUIRED
                }
                .compileInlining()

            result.json.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "required": [
                        "name"
                      ],
                      "properties": {
                        "description": {
                          "type": "string"
                        },
                        "name": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent()
            }
        }

        "kotlinx-serialization" {
            val result = typeOf<TestClassIssue16>()
                .processKotlinxSerialization()
                .generateJsonSchema {
                    optionalHandling = OptionalHandling.NON_REQUIRED
                }
                .compileInlining()

            result.json.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "required": [
                        "name"
                      ],
                      "properties": {
                        "name": {
                          "type": "string"
                        },
                        "description": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent()
            }
        }

    }


    "https://github.com/SMILEY4/schema-kenerator/issues/19 - required annotation not working when all props nullable or optional" - {

        "json" {
            val result = typeOf<TestClassIssue19>()
                .processKotlinxSerialization()
                .generateJsonSchema()
                .handleCoreAnnotations()
                .compileInlining()

            result.json.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "required": [
                        "prop1"
                      ],
                      "properties": {
                        "prop1": {
                          "type": "string"
                        },
                        "prop2": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent()
            }
        }

        "swagger" {
            val result = typeOf<TestClassIssue19>()
                .processKotlinxSerialization()
                .generateSwaggerSchema()
                .handleCoreAnnotations()
                .compileInlining()

            result.swagger.shouldEqualJson {
                """
                    {
                      "required": [
                        "prop1"
                      ],
                      "type": "object",
                      "properties": {
                        "prop1": {
                          "type": ["string", "null"]
                        },
                        "prop2": {
                          "type": ["string", "null"]
                        }
                      }
                    }
                """.trimIndent()
            }
        }

    }

    "https://github.com/SMILEY4/schema-kenerator/issues/20 - include annotations from constructor parameters" {
        val result = typeOf<TestClassIssue20>()
            .processReflection()
            .handleJacksonAnnotations()
            .generateSwaggerSchema()
            .handleJavaxValidationAnnotations()
            .compileInlining()

        result.swagger.shouldEqualJson {
            """
                {
                  "required": [ "passwordRenamed", "usernameRenamed" ],
                  "type": "object",
                  "properties": {
                    "passwordRenamed": {
                      "maxLength": 200,
                      "type": "string"
                    },
                    "usernameRenamed": {
                      "maxLength": 100,
                      "type": "string"
                    }
                  }
                }
            """.trimIndent()
        }
    }

    "https://github.com/SMILEY4/schema-kenerator/issues/18 - support renaming properties" - {

        "custom renameing (adding prefix)" {
            val result = typeOf<TestClassIssue18>()
                .processKotlinxSerialization()
                .renameMembers { name -> "prefix_$name" }
                .generateSwaggerSchema()
                .handleCoreAnnotations()
                .compileInlining()

            result.swagger.shouldEqualJson {
                """
                    {
                      "required": [ "prefix_nameOfPerson", "prefix_numberOfYears" ],
                      "type": "object",
                      "properties": {
                        "prefix_nameOfPerson": {
                          "type": "string"
                        },
                        "prefix_numberOfYears": {
                          "type": "integer",
                          "format": "int32"
                        }
                      }
                    }
                """.trimIndent()
            }
        }

        "kotlinx naming strategy (snake case)" {
            val result = typeOf<TestClassIssue18>()
                .processKotlinxSerialization()
                .renameMembers(JsonNamingStrategy.SnakeCase)
                .generateSwaggerSchema()
                .handleCoreAnnotations()
                .compileInlining()



            result.swagger.shouldEqualJson {
                """
                    {
                      "required": [ "name_of_person", "number_of_years" ],
                      "type": "object",
                      "properties": {
                        "name_of_person": {
                          "type": "string"
                        },
                        "number_of_years": {
                          "type": "integer",
                          "format": "int32"
                        }
                      }
                    }
                """.trimIndent()
            }
        }

    }

    "customize property with shared type results in output schema with both all properties being modified" {

        class TestClass(
            val describeMe: String,
            val otherProperty: String
        )

        val result = typeOf<TestClass>()
            .processReflection()
            .generateJsonSchema()
            .customizeProperties { propertyData, propertySchema ->
                if (propertyData.name == "describeMe" && propertySchema is JsonObject) {
                    propertySchema.properties["description"] = JsonTextValue("test description")
                }
            }
            .compileInlining()

        result.json.shouldEqualJson {
            """
                {
                   "type": "object",
                   "required": [
                      "describeMe",
                      "otherProperty"
                   ],
                   "properties": {
                      "describeMe": {
                         "type": "string",
                         "description": "test description"
                      },
                      "otherProperty": {
                         "type": "string"
                      }
                   }
                }
            """.trimIndent()
        }
    }

    "https://github.com/SMILEY4/schema-kenerator/issues/39 - nullable property of sealed class" - {

        "inlining" {
            val result = typeOf<BIssue39>()
                .processReflection()
                .generateSwaggerSchema()
                .withTitle(TitleType.SIMPLE)
                .compileInlining()

            result.swagger.shouldEqualJson {
                """
                {
                  "type": "object",
                  "properties": {
                    "a": {
                      "anyOf": [
                        {
                          "type": "object",
                          "properties": {},
                          "title": "AIssue39"
                        },
                        {
                          "type": "null"
                        }
                      ],
                      "title": "SealedClassIssue39"
                    }
                  },
                  "title": "BIssue39"
                }
                """.trimIndent()
            }
        }

        "referencing" {
            val result = typeOf<BIssue39>()
                .processReflection()
                .generateSwaggerSchema()
                .withTitle(TitleType.SIMPLE)
                .compileReferencingRoot()

            (result.swagger to result.componentSchemas).shouldEqualJson {
                mapOf(
                    "." to """
                        {
                          "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.MiscTests.Companion.BIssue39"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.MiscTests.Companion.BIssue39" to """
                        {
                          "type": "object",
                          "properties": {
                            "a": {
                              "oneOf": [
                                {
                                  "type": "null"
                                },
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.MiscTests.Companion.SealedClassIssue39"
                                }
                              ]
                            }
                          },
                          "title": "BIssue39"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.MiscTests.Companion.SealedClassIssue39" to """
                        {
                          "anyOf": [
                            {
                              "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.MiscTests.Companion.AIssue39"
                            }
                          ],
                          "title": "SealedClassIssue39"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.MiscTests.Companion.AIssue39" to """
                        {
                          "type": "object",
                          "properties": {},
                          "title": "AIssue39"
                        }
                    """.trimIndent(),
                )
            }
        }

    }

    "copy swagger field 'type' to 'types'" - {

        "inlining" {

            val result = Bundle(
                data = SwaggerSchema(
                    swagger = Schema<Any>().also {
                        it.type = "myType"
                    },
                    typeData = TypeData.createWildcard()
                ),
                supporting = emptyList()
            ).compileInlining()

            result.swagger.shouldEqualJson {
                """
                {
                  "type": "myType"
                }
                """.trimIndent()
            }
        }

        "referencing" {
            val result = Bundle(
                data = SwaggerSchema(
                    swagger = Schema<Any>().also {
                        it.type = "myType"
                    },
                    typeData = TypeData.createWildcard()
                ),
                supporting = emptyList()
            ).compileReferencingRoot()

            (result.swagger to result.componentSchemas).shouldEqualJson {
                mapOf(
                    "." to """
                        {
                            "type": "myType"
                        }
                    """.trimIndent(),
                )
            }
        }

    }

    "merge property attributes with referenced type" {

        val result = typeOf<ClassWithAnnotatedFields>()
            .processReflection()
            .generateSwaggerSchema()
            .handleCoreAnnotations()
            .mergePropertyAttributesIntoType()
            .compileReferencingRoot()

        val componentSchemasCleanIds: Map<String, Schema<*>> = result.componentSchemas
            .map { (key, value) ->
                if (key.startsWith("io.github.smiley4.schemakenerator.test.MiscTests.Companion.TestClassIssue18")) {
                    if (value.types.contains("type-a")) {
                        return@map "io.github.smiley4.schemakenerator.test.MiscTests.Companion.TestClassIssue18#A" to value
                    }
                    if (value.types.contains("type-b")) {
                        return@map "io.github.smiley4.schemakenerator.test.MiscTests.Companion.TestClassIssue18#B" to value
                    }
                }
                if (key.startsWith("io.github.smiley4.schemakenerator.test.MiscTests.Companion.ClassWithAnnotatedFields")) {
                    value.properties["fieldA"]?.`$ref` = "io.github.smiley4.schemakenerator.test.MiscTests.Companion.TestClassIssue18#A"
                    value.properties["fieldB"]?.`$ref` = "io.github.smiley4.schemakenerator.test.MiscTests.Companion.TestClassIssue18#B"
                }
                key to value
            }
            .associate { it }

        (result.swagger to componentSchemasCleanIds).shouldEqualJson {
            mapOf(
                "." to """
                    {
                      "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.MiscTests.Companion.ClassWithAnnotatedFields"
                    }
                """.trimIndent(),
                "io.github.smiley4.schemakenerator.test.MiscTests.Companion.ClassWithAnnotatedFields" to """
                    {
                      "type": "object",
                      "properties": {
                        "fieldA": {
                          "${'$'}ref": "io.github.smiley4.schemakenerator.test.MiscTests.Companion.TestClassIssue18#A"
                        },
                        "fieldB": {
                          "${'$'}ref": "io.github.smiley4.schemakenerator.test.MiscTests.Companion.TestClassIssue18#B"
                        }
                      },
                      "required": [
                        "fieldA",
                        "fieldB"
                      ]
                    }
                """.trimIndent(),
                "io.github.smiley4.schemakenerator.test.MiscTests.Companion.TestClassIssue18#A" to """
                    {
                      "type": [
                        "object",
                        "type-a"
                      ],
                      "format": "format-a",
                      "properties": {
                        "nameOfPerson": {
                          "type": "string"
                        },
                        "numberOfYears": {
                          "type": "integer",
                          "format": "int32"
                        }
                      },
                      "required": [
                        "nameOfPerson",
                        "numberOfYears"
                      ]
                    }
                """.trimIndent(),
                "io.github.smiley4.schemakenerator.test.MiscTests.Companion.TestClassIssue18#B" to """
                    {
                      "type": [
                        "object",
                        "type-b"
                      ],
                      "format": "format-b",
                      "properties": {
                        "nameOfPerson": {
                          "type": "string"
                        },
                        "numberOfYears": {
                          "type": "integer",
                          "format": "int32"
                        }
                      },
                      "required": [
                        "nameOfPerson",
                        "numberOfYears"
                      ]
                    }
                """.trimIndent()
            )
        }

    }

    "generic nested classes with nullable type parameter" - {

        "reflection" {
            val result = typeOf<GenericClass<String?>>()
                .processReflection()
                .generateSwaggerSchema()
                .compileInlining()
            result.swagger.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "properties": {
                        "nested": {
                          "type": "object",
                          "properties": {
                            "value": {
                              "type": [
                                "null",
                                "string"
                              ]
                            }
                          }
                        }
                      },
                      "required": [
                        "nested"
                      ]
                    }
                """.trimIndent()
            }
        }

        "kotlinx-serialization" {
            val result = typeOf<GenericClass<String?>>()
                .processKotlinxSerialization()
                .generateSwaggerSchema()
                .compileInlining()
            result.swagger.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "properties": {
                        "nested": {
                          "type": "object",
                          "properties": {
                            "value": {
                              "type": [
                                "null",
                                "string"
                              ]
                            }
                          }
                        }
                      },
                      "required": [
                        "nested"
                      ]
                    }
                """.trimIndent()
            }
        }

    }

    "kotlinx contextual" - {


        "with serializers from config" {

            val json = Json {
                serializersModule = SerializersModule {
                    contextual(UUID::class, MyUUIDSerializer)
                    contextual(Instant::class, MyInstantSerializer)
                }
            }

            val result = typeOf<TestClassContextual>()
                .processKotlinxSerialization {
                    serializersModule = json.serializersModule
                }
                .generateSwaggerSchema()
                .withTitle(TitleType.SIMPLE)
                .compileInlining()

            result.swagger.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "properties": {
                        "timestamp": {
                          "type": "integer",
                          "format": "int64",
                          "title": "Instant"
                        },
                        "id": {
                          "type": "string",
                          "title": "UUID"
                        }
                      },
                      "required": [
                        "id",
                        "timestamp"
                      ],
                      "title": "TestClassContextual"
                    }
                """.trimIndent()
            }
        }

        "with serializers from annotation" {

            val result = typeOf<TestClassSerializableWith>()
                .processKotlinxSerialization {}
                .generateSwaggerSchema()
                .withTitle(TitleType.SIMPLE)
                .compileInlining()

            result.swagger.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "properties": {
                        "timestamp": {
                          "type": "integer",
                          "format": "int64",
                          "title": "Instant"
                        },
                        "id": {
                          "type": "string",
                          "title": "UUID"
                        }
                      },
                      "required": [
                        "id",
                        "timestamp"
                      ],
                      "title": "TestClassSerializableWith"
                    }
                """.trimIndent()
            }
        }

    }

}) {

    companion object {

        class TestClassIssue14a(
            val name: Optional<String?>
        )


        @Serializable
        class TestClassIssue14b(
            val name: Int
        )


        @Serializable
        data class TestClassIssue16(
            val name: String,
            val description: String? = null
        )


        @Serializable
        data class TestClassIssue19(
            @Required
            val prop1: String?,
            val prop2: String? = null
        )

        data class TestClassIssue20(
            @field:Size(max = 100)
            @JsonProperty("usernameRenamed", required = true)
            val username: String?,

            @field:Size(max = 200)
            @JsonProperty("passwordRenamed", required = true)
            val password: String?
        )


        @Serializable
        data class TestClassIssue18(
            val nameOfPerson: String,
            val numberOfYears: Int
        )

        sealed class SealedClassIssue39

        class AIssue39 : SealedClassIssue39()

        class BIssue39(val a: SealedClassIssue39?)


        data class ClassWithAnnotatedFields(
            @Format("format-a")
            @Type("type-a")
            val fieldA: TestClassIssue18,
            @Format("format-b")
            @Type("type-b")
            val fieldB: TestClassIssue18
        )


        @Serializable
        data class GenericClass<T>(val nested: NestedGenericClass<T>)


        @Serializable
        data class NestedGenericClass<T>(val value: T)



        @Serializable
        data class TestClassContextual(
            @Contextual
            val timestamp: Instant,
            @Contextual
            val id: UUID,
        )

        @Serializable
        data class TestClassSerializableWith(
            @Serializable(with = MyInstantSerializer::class)
            val timestamp: Instant,
            @Serializable(with = MyUUIDSerializer::class)
            val id: UUID,
        )


        object MyInstantSerializer : KSerializer<Instant> {
            override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.time.Instant", PrimitiveKind.LONG)
            override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeLong(value.toEpochMilli())
            override fun deserialize(decoder: Decoder): Instant = Instant.ofEpochMilli(decoder.decodeLong())
        }

        object MyUUIDSerializer : KSerializer<UUID> {
            override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
            override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
            override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
        }

    }

}