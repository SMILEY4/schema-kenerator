package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.CoreSteps.initial
import io.github.smiley4.schemakenerator.core.data.InitialKTypeData
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.generateJsonSchema
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.analyzeTypeUsingReflection
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.analyzeTypeUsingKotlinxSerialization
import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

class RedirectTests : FreeSpec({

    "recursive redirects" - {

        "reflection" {
            val result = initial<TestClass>()
                .analyzeTypeUsingReflection {
                    redirect<NestedClass, String>()
                    redirect<String, Int>()
                }
                .generateJsonSchema()
                .compileInlining()

            result.json.shouldEqualJson {
                """
                    {
                       "type": "object",
                       "required": [
                          "data"
                       ],
                       "properties": {
                          "data": {
                             "type": "integer",
                             "minimum": -2147483648,
                             "maximum": 2147483647
                          }
                       }
                    }
                """.trimIndent()
            }
        }

        "kotlinx-serialization" {
            val result = initial<TestClass>()
                .analyzeTypeUsingKotlinxSerialization {
                    redirect<NestedClass, String>()
                    redirect<String, Int>()
                }
                .generateJsonSchema()
                .compileInlining()

            result.json.shouldEqualJson {
                """
                    {
                       "type": "object",
                       "required": [
                          "data"
                       ],
                       "properties": {
                          "data": {
                             "type": "integer",
                             "minimum": -2147483648,
                             "maximum": 2147483647
                          }
                       }
                    }
                """.trimIndent()
            }
        }

    }

    "redirect to nullable" - {

        "reflection" {
            val result = initial<TestClass>()
                .analyzeTypeUsingReflection {
                    redirect<String, String?>()
                }
                .generateJsonSchema()
                .compileInlining()

            result.json.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "required": [
                        "data"
                      ],
                      "properties": {
                        "data": {
                          "type": "object",
                          "required": [
                            "someNumber"
                          ],
                          "properties": {
                            "someNumber": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647
                            },
                            "someText": {
                              "type": "string"
                            }
                          }
                        }
                      }
                    }
                """.trimIndent()
            }
        }

        "kotlinx-serialization" {
            val result = initial<TestClass>()
                .analyzeTypeUsingKotlinxSerialization {
                    redirect<String, String?>()
                }
                .generateJsonSchema()
                .compileInlining()

            result.json.shouldEqualJson {
                """
                    {
                      "type": "object",
                      "required": [
                        "data"
                      ],
                      "properties": {
                        "data": {
                          "type": "object",
                          "required": [
                            "someNumber"
                          ],
                          "properties": {
                            "someNumber": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647
                            },
                            "someText": {
                              "type": "string"
                            }
                          }
                        }
                      }
                    }
                """.trimIndent()
            }
        }

    }

}) {

    companion object {

        @Serializable
        class TestClass(
            val data: NestedClass
        )

        @Serializable
        class NestedClass(
            val someText: String,
            val someNumber: Int
        )

    }

}