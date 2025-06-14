@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.smiley4.schemakenerator.core.CoreSteps.addMissingSupertypeSubtypeRelations
import io.github.smiley4.schemakenerator.core.CoreSteps.initial
import io.github.smiley4.schemakenerator.core.CoreSteps.renameMembers
import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.annotations.Format
import io.github.smiley4.schemakenerator.core.annotations.Required
import io.github.smiley4.schemakenerator.core.annotations.Type
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.TypeId
import io.github.smiley4.schemakenerator.jackson.JacksonSteps.handleJacksonAnnotations
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.customizeProperties
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.handleCoreAnnotations
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.analyzeTypeUsingReflection
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.collectSubTypes
import io.github.smiley4.schemakenerator.reflection.data.TypeRedirect
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.analyzeTypeUsingKotlinxSerialization
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.renameMembers
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.compileInlining
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.mergePropertyAttributesIntoType
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps.withTitle
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.SwaggerSchemaData
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.validation.swagger.ValidationSwaggerSteps.handleJavaxValidationAnnotations
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
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
import kotlinx.serialization.modules.contextual
import java.time.Instant
import java.util.Optional
import java.util.UUID
import javax.validation.constraints.Size

class MiscTests : FreeSpec({

    "https://github.com/SMILEY4/schema-kenerator/issues/14 - redirect to nullable types" - {

        "reflection" {
            val result = initial<TestClassIssue14a>()
                .analyzeTypeUsingReflection {
                    redirect {
                        from<Optional<String?>>()
                        to<String?>(TypeRedirect.ToNullability.REPLACE)
                    }
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
            val result = initial<TestClassIssue14b>()
                .analyzeTypeUsingKotlinxSerialization {
                    redirect {
                        from<Int>()
                        to<String?>(io.github.smiley4.schemakenerator.serialization.data.TypeRedirect.ToNullability.REPLACE)
                    }
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
            val result = initial<TestClassIssue16>()
                .analyzeTypeUsingReflection()
                .generateJsonSchema {
                    optionalHandling = JsonSchemaSteps.OptionalHandling.NON_REQUIRED
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
            val result = initial<TestClassIssue16>()
                .analyzeTypeUsingKotlinxSerialization()
                .generateJsonSchema {
                    optionalHandling = JsonSchemaSteps.OptionalHandling.NON_REQUIRED
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
            val result = initial<TestClassIssue19>()
                .analyzeTypeUsingKotlinxSerialization()
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
            val result = initial<TestClassIssue19>()
                .analyzeTypeUsingKotlinxSerialization()
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
        val result = initial<TestClassIssue20>()
            .analyzeTypeUsingReflection()
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
            val result = initial<TestClassIssue18>()
                .analyzeTypeUsingKotlinxSerialization()
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
            val result = initial<TestClassIssue18>()
                .analyzeTypeUsingKotlinxSerialization()
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

        val result = initial<TestClass>()
            .analyzeTypeUsingReflection()
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
            val result = initial<BIssue39>()
                .analyzeTypeUsingReflection()
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
            val result = initial<BIssue39>()
                .analyzeTypeUsingReflection()
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

            val result = IntermediateSwaggerSchemaData(
                rootId = TypeId("test"),
                data = mapOf(
                    TypeId("test") to SwaggerSchemaData(
                        swagger = Schema<Any>().also {
                            it.type = "myType"
                        },
                        typeData = TypeData.createWildcard()
                    )
                )
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
            val result = IntermediateSwaggerSchemaData(
                rootId = TypeId("test"),
                data = mapOf(
                    TypeId("test") to SwaggerSchemaData(
                        swagger = Schema<Any>().also {
                            it.type = "myType"
                        },
                        typeData = TypeData.createWildcard()
                    )
                )
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

        val result = initial<ClassWithAnnotatedFields>()
            .analyzeTypeUsingReflection()
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
            val result = initial<GenericClass<String?>>()
                .analyzeTypeUsingReflection()
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
            val result = initial<GenericClass<String?>>()
                .analyzeTypeUsingKotlinxSerialization()
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

            val result = initial<TestClassContextual>()
                .analyzeTypeUsingKotlinxSerialization {
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

            val result = initial<TestClassSerializableWith>()
                .analyzeTypeUsingKotlinxSerialization {}
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

    "https://github.com/SMILEY4/schema-kenerator/issues/43" - {

        "overwriting property with more specific type" {
            val result = initial<Issue43IntHolder>()
                .analyzeTypeUsingReflection()
                .generateSwaggerSchema()
                .withTitle(TitleType.SIMPLE)
                .compileReferencingRoot()
            (result.swagger to result.componentSchemas).shouldEqualJson {
                mapOf(
                    "." to """
                        {
                          "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.MiscTests.Companion.Issue43IntHolder"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.MiscTests.Companion.Issue43IntHolder" to """
                        {
                          "type" : "object",
                          "properties" : {
                            "value" : {
                              "type" : "integer",
                              "format" : "int32",
                              "title" : "Int"
                            }
                          },
                          "required" : [ "value" ],
                          "title" : "Issue43IntHolder"
                        }
                    """.trimIndent(),
                )
            }
        }

        "collect correct subtypes with type parameters involved" {
            val result = initial<Issue43Root>()
                .collectSubTypes()
                .analyzeTypeUsingReflection()
                .addMissingSupertypeSubtypeRelations()
                .also { data ->
                    data.typeData
                        .find { it.identifyingName.full == "io.github.smiley4.schemakenerator.test.MiscTests.Companion.Issue42Interface.WithEnum" }!!
                        .also { withEnum ->
                            val supertypes = withEnum.supertypes
                                .map { data[it]!! }
                                .map { it to it.typeParameters.map { t -> data[t.type] } }
                            supertypes shouldHaveSize 1
                            supertypes.map { it.first.descriptiveName.short + " " + it.second.joinToString { t -> t!!.descriptiveName.short } } shouldContainExactlyInAnyOrder
                                    listOf("Issue42Interface Issue43Enum")
                        }
                    data.typeData
                        .find { it.identifyingName.full == "io.github.smiley4.schemakenerator.test.MiscTests.Companion.Issue42Interface.WithInt" }!!
                        .also { withEnum ->
                            val supertypes = withEnum.supertypes
                                .map { data[it]!! }
                                .map { it to it.typeParameters.map { t -> data[t.type] } }
                            supertypes shouldHaveSize 1
                            supertypes.map { it.first.descriptiveName.short + " " + it.second.joinToString { t -> t!!.descriptiveName.short } } shouldContainExactlyInAnyOrder
                                    listOf("Issue42Interface Int")
                        }
                }
                .generateSwaggerSchema()
                .withTitle(TitleType.SIMPLE)
                .compileReferencingRoot()

            (result.swagger to result.componentSchemas).shouldEqualJson {
                mapOf(
                    "." to """
                        {
                          "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.MiscTests.Companion.Issue43Root"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.MiscTests.Companion.Issue43Root" to """
                        {
                          "type": "object",
                          "properties": {
                            "withEnum": {
                              "oneOf": [
                                {
                                  "type": "null"
                                },
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.MiscTests.Companion.Issue42Interface.WithEnum"
                                }
                              ]
                            },
                            "withInt": {
                              "oneOf": [
                                {
                                  "type": "null"
                                },
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.MiscTests.Companion.Issue42Interface.WithInt"
                                }
                              ]
                            }
                          },
                          "title": "Issue43Root"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.MiscTests.Companion.Issue42Interface.WithEnum" to """
                        {
                          "type": "object",
                          "properties": {
                            "data": {
                              "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.MiscTests.Companion.Issue43Enum"
                            }
                          },
                          "required": [
                            "data"
                          ],
                          "title": "WithEnum"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.MiscTests.Companion.Issue42Interface.WithInt" to """
                        {
                          "type": "object",
                          "properties": {
                            "data": {
                              "type": "integer",
                              "format": "int32",
                              "title": "Int"
                            }
                          },
                          "required": [
                            "data"
                          ],
                          "title": "WithInt"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.MiscTests.Companion.Issue43Enum" to """
                        {
                          "type": "string",
                          "enum": [
                            "Alpha",
                            "Beta"
                          ],
                          "title": "Issue43Enum"
                        }
                    """.trimIndent(),
                )
            }
        }
    }

    "nullable types" - {

        "with explicit null types, nullables as non-required" {
            val result = initial<ClassWithNullableFields>()
                .analyzeTypeUsingKotlinxSerialization {}
                .generateSwaggerSchema()
                .withTitle(TitleType.SIMPLE)
                .compileInlining(explicitNullTypes = true)
            result.swagger.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "properties": {
                        "nonNull": {
                          "type": "string",
                          "title": "String"
                        },
                        "nullable": {
                          "type": [
                            "null",
                            "string"
                          ],
                          "title": "String"
                        },
                        "nonNullWithDefault": {
                          "type": "string",
                          "title": "String"
                        },
                        "nullableWithDefault": {
                          "type": [
                            "null",
                            "string"
                          ],
                          "title": "String"
                        }
                      },
                      "required": [
                        "nonNull",
                        "nonNullWithDefault"
                      ],
                      "title": "ClassWithNullableFields"
                    }
                """.trimIndent()
            }
        }

        "without explicit null types, nullables as non-required" {
            val result = initial<ClassWithNullableFields>()
                .analyzeTypeUsingKotlinxSerialization {}
                .generateSwaggerSchema()
                .withTitle(TitleType.SIMPLE)
                .compileInlining(explicitNullTypes = false)
            result.swagger.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "properties": {
                        "nonNull": {
                          "type": "string",
                          "title": "String"
                        },
                        "nullable": {
                          "type": "string",
                          "title": "String"
                        },
                        "nonNullWithDefault": {
                          "type": "string",
                          "title": "String"
                        },
                        "nullableWithDefault": {
                          "type": "string",
                          "title": "String"
                        }
                      },
                      "required": [
                        "nonNull",
                        "nonNullWithDefault"
                      ],
                      "title": "ClassWithNullableFields"
                    }
                """.trimIndent()
            }
        }

        "with explicit null types, nullables as required" {
            val result = initial<ClassWithNullableFields>()
                .analyzeTypeUsingKotlinxSerialization {}
                .generateSwaggerSchema {
                    nullables = SwaggerSteps.RequiredHandling.REQUIRED
                }
                .withTitle(TitleType.SIMPLE)
                .compileInlining(explicitNullTypes = true)
            result.swagger.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "properties": {
                        "nonNull": {
                          "type": "string",
                          "title": "String"
                        },
                        "nullable": {
                          "type": [
                            "null",
                            "string"
                          ],
                          "title": "String"
                        },
                        "nonNullWithDefault": {
                          "type": "string",
                          "title": "String"
                        },
                        "nullableWithDefault": {
                          "type": [
                            "null",
                            "string"
                          ],
                          "title": "String"
                        }
                      },
                      "required": [
                        "nonNull",
                        "nullable",
                        "nonNullWithDefault",
                        "nullableWithDefault"
                      ],
                      "title": "ClassWithNullableFields"
                    }
                """.trimIndent()
            }
        }

        "without explicit null types, nullables as required" {
            val result = initial<ClassWithNullableFields>()
                .analyzeTypeUsingKotlinxSerialization {}
                .generateSwaggerSchema { nullables = SwaggerSteps.RequiredHandling.REQUIRED }
                .withTitle(TitleType.SIMPLE)
                .compileInlining(explicitNullTypes = false)
            result.swagger.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "properties": {
                        "nonNull": {
                          "type": "string",
                          "title": "String"
                        },
                        "nullable": {
                          "type": "string",
                          "title": "String"
                        },
                        "nonNullWithDefault": {
                          "type": "string",
                          "title": "String"
                        },
                        "nullableWithDefault": {
                          "type": "string",
                          "title": "String"
                        }
                      },
                      "required": [
                        "nonNull",
                        "nullable",
                        "nonNullWithDefault",
                        "nullableWithDefault"
                      ],
                      "title": "ClassWithNullableFields"
                    }
                """.trimIndent()
            }
        }
    }

    "special floating point values" - {

        "don't allow" {
            val result = initial<ClassWithNumbers>()
                .analyzeTypeUsingReflection()
                .generateSwaggerSchema {
                    allowSpecialFloatingPointValues = false
                }
                .compileInlining()
            result.swagger.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "properties": {
                        "doubleValue": {
                          "type": "number",
                          "format": "double"
                        },
                        "floatValue": {
                          "type": "number",
                          "format": "float"
                        },
                        "intValue": {
                          "type": "integer",
                          "format": "int32"
                        },
                        "numberValue": {
                          "type": "number"
                        }
                      },
                      "required": [
                        "doubleValue",
                        "floatValue",
                        "intValue",
                        "numberValue"
                      ]
                    }
                """.trimIndent()
            }
        }

        "allow" {
            val result = initial<ClassWithNumbers>()
                .analyzeTypeUsingReflection {}
                .generateSwaggerSchema {
                    allowSpecialFloatingPointValues = true
                }
                .compileInlining()
            result.swagger.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "properties": {
                        "doubleValue": {
                          "anyOf": [
                            {
                              "type": "number",
                              "format": "double"
                            },
                            {
                              "enum": [
                                "NaN",
                                "Infinity",
                                "-Infinity"
                              ]
                            }
                          ]
                        },
                        "floatValue": {
                          "anyOf": [
                            {
                              "type": "number",
                              "format": "float"
                            },
                            {
                              "enum": [
                                "NaN",
                                "Infinity",
                                "-Infinity"
                              ]
                            }
                          ]
                        },
                        "intValue": {
                          "type": "integer",
                          "format": "int32"
                        },
                        "numberValue": {
                          "type": "number"
                        }
                      },
                      "required": [
                        "doubleValue",
                        "floatValue",
                        "intValue",
                        "numberValue"
                      ]
                    }
                """.trimIndent()
            }
        }
    }

    "maps with complex keys as arrays" - {

        "disabled" {
            val result = initial<ClassWithCombinedKeyMap>()
                .analyzeTypeUsingKotlinxSerialization()
                .generateSwaggerSchema {
                    mapsWithStructuredKeysAsArrays = false
                }
                .compileInlining()
            result.swagger.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "properties": {
                        "map": {
                          "type": "object",
                          "additionalProperties": {
                            "type": "integer",
                            "format": "int32"
                          }
                        }
                      },
                      "required": [
                        "map"
                      ]
                    }
                """.trimIndent()
            }
        }

        "enabled" {
            val result = initial<ClassWithCombinedKeyMap>()
                .analyzeTypeUsingKotlinxSerialization {}
                .generateSwaggerSchema {
                    mapsWithStructuredKeysAsArrays = true
                }
                .compileInlining()
            result.swagger.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "properties": {
                        "map": {
                          "type": "array",
                          "items": {
                            "anyOf": [
                              {
                                "type": "object",
                                "properties": {
                                  "a": {
                                    "type": "string"
                                  },
                                  "b": {
                                    "type": "integer",
                                    "format": "int32"
                                  }
                                },
                                "required": [
                                  "a",
                                  "b"
                                ]
                              },
                              {
                                "type": "integer",
                                "format": "int32"
                              }
                            ]
                          }
                        }
                      },
                      "required": [
                        "map"
                      ]
                    }
                """.trimIndent()
            }
        }

    }

    "class with multiple @Contextual annotations" - {
        val result = initial<ClassMultipleContextuals>()
            .analyzeTypeUsingKotlinxSerialization {
                serializersModule = SerializersModule {
                    contextual(MyInstantSerializer)
                }
            }
            .generateSwaggerSchema()
            .compileInlining()
        result.swagger.shouldEqualJson {
            """
                {
                  "type": "object",
                  "properties": {
                    "fieldA": {
                      "type": [
                        "null",
                        "integer"
                      ],
                      "format": "int64"
                    },
                    "fieldB": {
                      "type": [
                        "null",
                        "integer"
                      ],
                      "format": "int64"
                    }
                  }
                }
            """.trimIndent()
        }
    }

    "description on property and class - https://github.com/SMILEY4/ktor-openapi-tools/issues/200" {

        val result = initial<ClassWithPropertyDescriptions>()
            .analyzeTypeUsingReflection()
            .generateSwaggerSchema()
            .handleCoreAnnotations()
            .mergePropertyAttributesIntoType()
            .compileReferencingRoot()

        (result.swagger to result.componentSchemas).shouldEqualJson {
            mapOf(
                "." to """
                    {
                      "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.MiscTests.Companion.ClassWithPropertyDescriptions"
                    }
                """.trimIndent(),
                "io.github.smiley4.schemakenerator.test.MiscTests.Companion.ClassWithPropertyDescriptions" to """
                    {
                      "type": "object",
                      "properties": {
                        "someProp": {
                          "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.MiscTests.Companion.ClassWithDescription",
                          "description": "description on property"
                        }
                      },
                      "required": [
                        "someProp"
                      ]
                    }
                """.trimIndent(),
                "io.github.smiley4.schemakenerator.test.MiscTests.Companion.ClassWithDescription" to """
                    {
                      "type": "object",
                      "description": "description on class",
                      "properties": {
                        "value": {
                          "type": "integer",
                          "format": "int32"
                        }
                      },
                      "required": [
                        "value"
                      ]
                    }
                """.trimIndent(),
            )
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

        interface Issue43SimpleInterface {
            val value: Number
        }

        data class Issue43IntHolder(
            override val value: Int
        ) : Issue43SimpleInterface

        data class Issue43Root(
            val withInt: Issue42Interface.WithInt?,
            val withEnum: Issue42Interface.WithEnum?,
        )

        sealed interface Issue42Interface<T> {
            val data: T

            data class WithInt(override val data: Int) : Issue42Interface<Int>
            data class WithEnum(override val data: Issue43Enum) : Issue42Interface<Issue43Enum>
        }

        enum class Issue43Enum { Alpha, Beta, }


        class ClassWithPropertyDescriptions(
            @Description("description on property") val someProp: ClassWithDescription
        )


        @Description("description on class")
        class ClassWithDescription(
            val value: Int
        )


        @Serializable
        data class ClassWithNullableFields(
            val nonNull: String,
            val nullable: String?,
            val nonNullWithDefault: String = "some value",
            val nullableWithDefault: String? = "some other value"
        )

        data class ClassWithNumbers(
            val floatValue: Float,
            val doubleValue: Double,
            val intValue: Int,
            val numberValue: Number,
        )


        @Serializable
        data class CombinedKey(val a: String, val b: Int)


        @Serializable
        data class ClassWithCombinedKeyMap(
            val map: Map<CombinedKey, Int>
        )


        @Serializable
        data class ClassMultipleContextuals(
            val fieldA: @Contextual Instant? = null,
            val fieldB: @Contextual Instant? = null,
        )

    }

}