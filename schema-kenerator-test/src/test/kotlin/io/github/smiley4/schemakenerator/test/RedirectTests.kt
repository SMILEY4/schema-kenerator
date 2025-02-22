package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.CoreSteps.initial
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.generateJsonSchema
import io.github.smiley4.schemakenerator.reflection.ReflectionSteps.analyzeTypeUsingReflection
import io.github.smiley4.schemakenerator.reflection.data.TypeRedirect as ReflectionTypeRedirect
import io.github.smiley4.schemakenerator.serialization.data.TypeRedirect as SerializationTypeRedirect
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.analyzeTypeUsingKotlinxSerialization
import io.kotest.core.spec.style.FreeSpec
import kotlinx.serialization.Serializable

class RedirectTests : FreeSpec({

    "nullability" - {

        "reflection" - {

            fun run(from: ReflectionTypeRedirect.FromNullability, to: ReflectionTypeRedirect.ToNullability, expected: () -> String) {
                val result = initial<SimpleTestClass>()
                    .analyzeTypeUsingReflection {
                        redirect {
                            from<String>(from)
                            to<Int?>(to)
                        }
                    }
                    .generateJsonSchema()
                    .compileInlining()
                result.json.shouldEqualJson { expected() }
            }

            "match, keep" {
                run(ReflectionTypeRedirect.FromNullability.MATCH, ReflectionTypeRedirect.ToNullability.KEEP) {
                    """
                        {
                          "type": "object",
                          "required": [
                            "requiredText"
                          ],
                          "properties": {
                            "nullableText": {
                              "type": "string"
                            },
                            "requiredText": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647
                            }
                          }
                        }
                    """.trimIndent()
                }
            }

            "match, replace" {
                run(ReflectionTypeRedirect.FromNullability.MATCH, ReflectionTypeRedirect.ToNullability.REPLACE) {
                    """
                        {
                          "type": "object",
                          "required": [],
                          "properties": {
                            "nullableText": {
                              "type": "string"
                            },
                            "requiredText": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647
                            }
                          }
                        }
                    """.trimIndent()
                }
            }

            "ignore, keep" {
                run(ReflectionTypeRedirect.FromNullability.IGNORE, ReflectionTypeRedirect.ToNullability.KEEP) {
                    """
                        {
                          "type": "object",
                          "required": [
                            "requiredText"
                          ],
                          "properties": {
                            "nullableText": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647
                            },
                            "requiredText": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647
                            }
                          }
                        }
                    """.trimIndent()
                }
            }

            "ignore, replace" {
                run(ReflectionTypeRedirect.FromNullability.IGNORE, ReflectionTypeRedirect.ToNullability.REPLACE) {
                    """
                        {
                          "type": "object",
                          "required": [],
                          "properties": {
                            "nullableText": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647
                            },
                            "requiredText": {
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

        "kotlinx" - {

            fun run(from: SerializationTypeRedirect.FromNullability, to: SerializationTypeRedirect.ToNullability, expected: () -> String) {
                val result = initial<SimpleTestClass>()
                    .analyzeTypeUsingKotlinxSerialization {
                        redirect {
                            from<String>(from)
                            to<Int?>(to)
                        }
                    }
                    .generateJsonSchema()
                    .compileInlining()
                result.json.shouldEqualJson { expected() }
            }

            "match, keep" {
                run(SerializationTypeRedirect.FromNullability.MATCH, SerializationTypeRedirect.ToNullability.KEEP) {
                    """
                        {
                          "type": "object",
                          "required": [
                            "requiredText"
                          ],
                          "properties": {
                            "nullableText": {
                              "type": "string"
                            },
                            "requiredText": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647
                            }
                          }
                        }
                    """.trimIndent()
                }
            }

            "match, replace" {
                run(SerializationTypeRedirect.FromNullability.MATCH, SerializationTypeRedirect.ToNullability.REPLACE) {
                    """
                        {
                          "type": "object",
                          "required": [],
                          "properties": {
                            "nullableText": {
                              "type": "string"
                            },
                            "requiredText": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647
                            }
                          }
                        }
                    """.trimIndent()
                }
            }

            "ignore, keep" {
                run(SerializationTypeRedirect.FromNullability.IGNORE, SerializationTypeRedirect.ToNullability.KEEP) {
                    """
                        {
                          "type": "object",
                          "required": [
                            "requiredText"
                          ],
                          "properties": {
                            "nullableText": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647
                            },
                            "requiredText": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647
                            }
                          }
                        }
                    """.trimIndent()
                }
            }

            "ignore, replace" {
                run(SerializationTypeRedirect.FromNullability.IGNORE, SerializationTypeRedirect.ToNullability.REPLACE) {
                    """
                        {
                          "type": "object",
                          "required": [],
                          "properties": {
                            "nullableText": {
                              "type": "integer",
                              "minimum": -2147483648,
                              "maximum": 2147483647
                            },
                            "requiredText": {
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

    }

    "recursive redirects" - {

        "reflection" {
            val result = initial<TestClass>()
                .analyzeTypeUsingReflection {
                    redirect {
                        from<NestedClass>()
                        to<String>()
                    }
                    redirect {
                        from<String>()
                        to<Int>()
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
                    redirect {
                        from<NestedClass>()
                        to<String>()
                    }
                    redirect {
                        from<String>()
                        to<Int>()
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

}) {

    companion object {

        @Serializable
        class SimpleTestClass(
            val requiredText: String,
            val nullableText: String?,
        )


        @Serializable
        class TestClass(
            val data: NestedClass?
        )


        @Serializable
        class NestedClass(
            val someText: String,
            val someNumber: Int
        )

    }

}