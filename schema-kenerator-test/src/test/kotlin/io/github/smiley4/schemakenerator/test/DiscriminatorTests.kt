package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smiley4.schemakenerator.core.addDiscriminatorProperty
import io.github.smiley4.schemakenerator.jackson.addJacksonTypeInfoDiscriminatorProperty
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.serialization.addJsonClassDiscriminatorProperty
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.compileReferencing
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.reflect.typeOf

class DiscriminatorTests : FreeSpec({

    "core only" - {

        "inlining" {
            val result = typeOf<TestClass>()
                .processReflection()
                .addDiscriminatorProperty("_type")
                .generateSwaggerSchema()
                .compileInlining()
            result.swagger.shouldEqualJson {
                """
                    {
                      "anyOf": [
                        {
                          "type": "object",
                          "properties": {
                            "_type": {
                              "type": "string"
                            }
                          },
                          "required": [
                            "_type"
                          ]
                        },
                        {
                          "type": "object",
                          "properties": {
                            "_type": {
                              "type": "string"
                            }
                          },
                          "required": [
                            "_type"
                          ]
                        }
                      ],
                      "discriminator": {
                        "propertyName": "_type"
                      }
                    }
                """.trimIndent()
            }
        }

        "referencing" - {

            "openapi simple path" {
                val result = typeOf<TestClass>()
                    .processReflection()
                    .addDiscriminatorProperty("_type")
                    .generateSwaggerSchema()
                    .compileReferencing(RefType.OPENAPI_SIMPLE)
                (result.swagger to result.componentSchemas).shouldEqualJson {
                    mapOf(
                        "." to """
                            {
                              "anyOf": [
                                {
                                  "${'$'}ref": "#/components/schemas/TestSubClass1"
                                },
                                {
                                  "${'$'}ref": "#/components/schemas/TestSubClass2"
                                }
                              ],
                              "discriminator": {
                                "propertyName": "_type",
                                "mapping": {
                                  "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass1": "#/components/schemas/TestSubClass1",
                                  "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass2": "#/components/schemas/TestSubClass2"
                                }
                              }
                            }
                        """.trimIndent(),
                        "TestSubClass1" to "...",
                        "TestSubClass2" to "...",
                    )
                }
            }

            "openapi full path" {
                val result = typeOf<TestClass>()
                    .processReflection()
                    .addDiscriminatorProperty("_type")
                    .generateSwaggerSchema()
                    .compileReferencing(RefType.OPENAPI_FULL)
                (result.swagger to result.componentSchemas).shouldEqualJson {
                    mapOf(
                        "." to """
                            {
                              "anyOf": [
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass1"
                                },
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass2"
                                }
                              ],
                              "discriminator": {
                                "propertyName": "_type",
                                "mapping": {
                                  "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass1": "#/components/schemas/io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass1",
                                  "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass2": "#/components/schemas/io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass2"
                                }
                              }
                            }
                        """.trimIndent(),
                        "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass1" to "...",
                        "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass2" to "..."
                    )
                }
            }

            "simple path" {
                val result = typeOf<TestClass>()
                    .processReflection()
                    .addDiscriminatorProperty("_type")
                    .generateSwaggerSchema()
                    .compileReferencing(RefType.OPENAPI_SIMPLE)
                (result.swagger to result.componentSchemas).shouldEqualJson {
                    mapOf(
                        "." to """
                            {
                              "anyOf": [
                                {
                                  "${'$'}ref": "#/components/schemas/TestSubClass1"
                                },
                                {
                                  "${'$'}ref": "#/components/schemas/TestSubClass2"
                                }
                              ],
                              "discriminator": {
                                "propertyName": "_type",
                                "mapping": {
                                  "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass1": "#/components/schemas/TestSubClass1",
                                  "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass2": "#/components/schemas/TestSubClass2"
                                }
                              }
                            }
                        """.trimIndent(),
                        "TestSubClass1" to "...",
                        "TestSubClass2" to "...",
                    )
                }
            }

            "full path" {
                val result = typeOf<TestClass>()
                    .processReflection()
                    .addDiscriminatorProperty("_type")
                    .generateSwaggerSchema()
                    .compileReferencing(RefType.FULL)
                (result.swagger to result.componentSchemas).shouldEqualJson {
                    mapOf(
                        "." to """
                            {
                              "anyOf": [
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass1"
                                },
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass2"
                                }
                              ],
                              "discriminator": {
                                "propertyName": "_type",
                                "mapping": {
                                  "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass1": "#/components/schemas/io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass1",
                                  "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass2": "#/components/schemas/io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass2"
                                }
                              }
                            }
                        """.trimIndent(),
                        "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass1" to "...",
                        "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.TestSubClass2" to "..."
                    )
                }
            }

        }

    }

    "with jackson annotations" - {

        "use class" {
            val result = typeOf<JacksonUseClassTestClass>()
                .processReflection()
                .addJacksonTypeInfoDiscriminatorProperty()
                .generateSwaggerSchema()
                .compileReferencing(RefType.OPENAPI_SIMPLE)
            (result.swagger to result.componentSchemas).shouldEqualJson {
                mapOf(
                    "." to """
                        {
                          "anyOf": [
                            {
                              "${'$'}ref": "#/components/schemas/JacksonUseClassTestSubClass1"
                            },
                            {
                              "${'$'}ref": "#/components/schemas/JacksonUseClassTestSubClass2"
                            }
                          ],
                          "discriminator": {
                            "propertyName": "_type",
                            "mapping": {
                              "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.JacksonUseClassTestSubClass1": "#/components/schemas/JacksonUseClassTestSubClass1",
                              "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.JacksonUseClassTestSubClass2": "#/components/schemas/JacksonUseClassTestSubClass2"
                            }
                          }
                        }
                    """.trimIndent(),
                    "JacksonUseClassTestSubClass1" to "...",
                    "JacksonUseClassTestSubClass2" to "...",
                )
            }
        }

        "use name" {
            val result = typeOf<JacksonUseNameTestClass>()
                .processReflection()
                .addJacksonTypeInfoDiscriminatorProperty()
                .generateSwaggerSchema()
                .compileReferencing(RefType.OPENAPI_SIMPLE)
            (result.swagger to result.componentSchemas).shouldEqualJson {
                mapOf(
                    "." to """
                        {
                          "anyOf": [
                            {
                              "${'$'}ref": "#/components/schemas/JacksonUseNameTestSubClass1"
                            },
                            {
                              "${'$'}ref": "#/components/schemas/JacksonUseNameTestSubClass2"
                            }
                          ],
                          "discriminator": {
                            "propertyName": "_type",
                            "mapping": {
                              "test_1": "#/components/schemas/JacksonUseNameTestSubClass1",
                              "test_2": "#/components/schemas/JacksonUseNameTestSubClass2"
                            }
                          }
                        }
                    """.trimIndent(),
                    "JacksonUseNameTestSubClass1" to "...",
                    "JacksonUseNameTestSubClass2" to "...",
                )
            }
        }

        "use simple name" {
            val result = typeOf<JacksonUseSimpleNameTestClass>()
                .processReflection()
                .addJacksonTypeInfoDiscriminatorProperty()
                .generateSwaggerSchema()
                .compileReferencing(RefType.OPENAPI_SIMPLE)
            (result.swagger to result.componentSchemas).shouldEqualJson {
                mapOf(
                    "." to """
                        {
                          "anyOf": [
                            {
                              "${'$'}ref": "#/components/schemas/JacksonUseSimpleNameTestSubClass1"
                            },
                            {
                              "${'$'}ref": "#/components/schemas/JacksonUseSimpleNameTestSubClass2"
                            }
                          ],
                          "discriminator": {
                            "propertyName": "_type",
                            "mapping": {
                              "JacksonUseSimpleNameTestSubClass1": "#/components/schemas/JacksonUseSimpleNameTestSubClass1",
                              "JacksonUseSimpleNameTestSubClass2": "#/components/schemas/JacksonUseSimpleNameTestSubClass2"
                            }
                          }
                        }
                    """.trimIndent(),
                    "JacksonUseSimpleNameTestSubClass1" to "...",
                    "JacksonUseSimpleNameTestSubClass2" to "...",
                )
            }
        }

    }

    "kotlinx-serialization" {
        val result = typeOf<KotlinxTestClass>()
            .processKotlinxSerialization()
            .addJsonClassDiscriminatorProperty()
            .generateSwaggerSchema()
            .compileReferencing(RefType.OPENAPI_SIMPLE)
        (result.swagger to result.componentSchemas).shouldEqualJson {
            mapOf(
                "." to """
                    {
                      "anyOf": [
                        {
                          "${'$'}ref": "#/components/schemas/KotlinxTestSubClass1"
                        },
                        {
                          "${'$'}ref": "#/components/schemas/test_2"
                        }
                      ],
                      "discriminator": {
                        "propertyName": "_myType",
                        "mapping": {
                          "io.github.smiley4.schemakenerator.test.DiscriminatorTests.Companion.KotlinxTestSubClass1": "#/components/schemas/KotlinxTestSubClass1",
                          "test_2": "#/components/schemas/test_2"
                        }
                      }
                    }
                    """.trimIndent(),
                "KotlinxTestSubClass1" to "...",
                "test_2" to "...",
            )
        }
    }

}) {

    companion object {

        private sealed class TestClass
        private class TestSubClass1 : TestClass()
        private class TestSubClass2 : TestClass()


        @JsonTypeInfo(
            property = "_type",
            include = JsonTypeInfo.As.PROPERTY,
            use = JsonTypeInfo.Id.CLASS,
        )
        private sealed class JacksonUseClassTestClass
        private class JacksonUseClassTestSubClass1 : JacksonUseClassTestClass()
        private class JacksonUseClassTestSubClass2 : JacksonUseClassTestClass()

        @JsonTypeInfo(
            property = "_type",
            include = JsonTypeInfo.As.PROPERTY,
            use = JsonTypeInfo.Id.NAME,
        )
        @JsonSubTypes(
            JsonSubTypes.Type(value = JacksonUseNameTestSubClass1::class, name = "test_1"),
            JsonSubTypes.Type(value = JacksonUseNameTestSubClass2::class, name = "test_2"),
        )
        private sealed class JacksonUseNameTestClass
        private class JacksonUseNameTestSubClass1 : JacksonUseNameTestClass()
        private class JacksonUseNameTestSubClass2 : JacksonUseNameTestClass()


        @JsonTypeInfo(
            property = "_type",
            include = JsonTypeInfo.As.PROPERTY,
            use = JsonTypeInfo.Id.SIMPLE_NAME,
        )
        private sealed class JacksonUseSimpleNameTestClass
        private class JacksonUseSimpleNameTestSubClass1 : JacksonUseSimpleNameTestClass()
        private class JacksonUseSimpleNameTestSubClass2 : JacksonUseSimpleNameTestClass()

        @OptIn(ExperimentalSerializationApi::class)
        @Serializable
        @JsonClassDiscriminator("_myType")
        private sealed class KotlinxTestClass

        @Serializable
        private class KotlinxTestSubClass1 : KotlinxTestClass()

        @Serializable
        @SerialName("test_2")
        private class KotlinxTestSubClass2 : KotlinxTestClass()


    }

}