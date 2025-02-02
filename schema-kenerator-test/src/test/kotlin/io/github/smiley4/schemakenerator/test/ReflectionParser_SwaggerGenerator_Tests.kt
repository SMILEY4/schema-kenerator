package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.reflection.analyseTypeUsingReflection
import io.github.smiley4.schemakenerator.swagger.OptionalHandling
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerationStepConfig
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.compileReferencing
import io.github.smiley4.schemakenerator.swagger.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.TitleBuilder
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.handleSchemaAnnotations
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithAnnotatedValueClass
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithCollections
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithDeepGeneric
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithNestedClass
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithSimpleFields
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithValueClass
import io.github.smiley4.schemakenerator.test.models.reflection.CoreAnnotatedClass
import io.github.smiley4.schemakenerator.test.models.reflection.SealedClass
import io.github.smiley4.schemakenerator.test.models.reflection.SubClassA
import io.github.smiley4.schemakenerator.test.models.reflection.TestEnum
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("ClassName")
class ReflectionParser_SwaggerGenerator_Tests : FunSpec({

    context("generator: inlining") {
        withData(TEST_DATA) { data ->

            val schema = data.type
                .analyseTypeUsingReflection()
                .generateSwaggerSchema(data.generatorConfig)
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .handleCoreAnnotations()
                            .handleSchemaAnnotations()
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list.withTitle(TitleBuilder.BUILDER_SIMPLE)
                    } else {
                        list
                    }
                }
                .compileInlining()
                .let {
                    Result(
                        schema = it.swagger,
                        definitions = emptyMap()
                    )
                }

            schema.schema.shouldEqualJson(data.expectedResultInlining)
        }
    }

    context("generator: referencing") {
        withData(TEST_DATA) { data ->

            val schema = data.type
                .analyseTypeUsingReflection()
                .generateSwaggerSchema(data.generatorConfig)
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .handleCoreAnnotations()
                            .handleSchemaAnnotations()
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list.withTitle(TitleBuilder.BUILDER_SIMPLE)
                    } else {
                        list
                    }
                }
                .compileReferencing(false, TitleBuilder.BUILDER_FULL)
                .let {
                    Result(
                        schema = it.swagger,
                        definitions = it.componentSchemas
                    )
                }

            (schema.schema to schema.definitions).shouldEqualJson(data.expectedResultReferencing)
        }
    }

    context("generator: referencing-root") {
        withData(TEST_DATA) { data ->

            val schema = data.type
                .analyseTypeUsingReflection()
                .generateSwaggerSchema(data.generatorConfig)
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .handleCoreAnnotations()
                            .handleSchemaAnnotations()
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list.withTitle(TitleBuilder.BUILDER_SIMPLE)
                    } else {
                        list
                    }
                }
                .compileReferencingRoot(false, TitleBuilder.BUILDER_FULL)
                .let {
                    Result(
                        schema = it.swagger,
                        definitions = it.componentSchemas
                    )
                }

            (schema.schema to schema.definitions).shouldEqualJson(data.expectedResultReferencingRoot)
        }
    }

}) {

    companion object {

        private data class Result(
            val schema: Schema<*>,
            val definitions: Map<String, Schema<*>>
        )

        private class TestData(
            val testName: String,
            val type: KType,
            val generatorConfig: SwaggerSchemaGenerationStepConfig.() -> Unit = {},
            val withAnnotations: Boolean = false,
            val withAutoTitle: Boolean = false,
            val expectedResultInlining: String,
            val expectedResultReferencing: Map<String, String>,
            val expectedResultReferencingRoot: Map<String, String>,
        ) : WithDataTestName {
            override fun dataTestName() = testName
        }

        private val TEST_DATA = listOf(
            TestData(
                type = typeOf<Any>(),
                testName = "any",
                expectedResultInlining = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                    {
                        "type": "object"
                    }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                    {
                        "type": "object"
                    }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<UByte>(),
                testName = "ubyte",
                expectedResultInlining = """
                    {
                        "type": "integer",
                        "maximum": 255,
                        "minimum": 0
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "integer",
                            "maximum": 255,
                            "minimum": 0
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "integer",
                            "maximum": 255,
                            "minimum": 0
                        }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<Int>(),
                testName = "int",
                expectedResultInlining = """
                    {
                        "type": "integer",
                        "format": "int32"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "integer",
                            "format": "int32"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "integer",
                            "format": "int32"
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<Float>(),
                testName = "float",
                expectedResultInlining = """
                    {
                        "type": "number",
                        "format": "float"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "number",
                            "format": "float"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "number",
                            "format": "float"
                        }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<Boolean>(),
                testName = "boolean",
                expectedResultInlining = """
                    {
                        "type": "boolean"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "boolean"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "boolean"
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<String>(),
                testName = "string",
                expectedResultInlining = """
                    {
                        "type": "string"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "string"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "string"
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<List<String>>(),
                testName = "list of strings",
                expectedResultInlining = """
                    {
                        "type": "array",
                        "items": {
                            "type": "string"
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "array",
                            "items": {
                                "type": "string"
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "type": "array",
                            "items": {
                                "type": "string"
                            }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<Map<String, Int>>(),
                testName = "map of strings to integers",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "additionalProperties": {
                            "type": "integer",
                            "format": "int32"
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "object",
                            "additionalProperties": {
                                "type": "integer",
                                "format": "int32"
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                    {
                        "type": "object",
                        "additionalProperties": {
                            "type": "integer",
                            "format": "int32"
                        }
                    }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<ClassWithSimpleFields>(),
                testName = "class with simple fields",
                expectedResultInlining = """
                    {
                        "required": [
                            "someBoolList",
                            "someString"
                        ],
                        "type": "object",
                        "properties": {
                            "someBoolList": {
                                "type": "array",
                                "items": {
                                  "type": "boolean"
                                }
                            },
                            "someNullableInt": {
                                "type": ["integer", "null"],
                                "format": "int32"
                            },
                            "someString": {
                                "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "required": [
                                "someBoolList",
                                "someString"
                            ],
                            "type": "object",
                            "properties": {
                                "someBoolList": {
                                    "type": "array",
                                    "items": {
                                      "type": "boolean"
                                    }
                                },
                                "someNullableInt": {
                                    "type": ["integer", "null"],
                                    "format": "int32"
                                },
                                "someString": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithSimpleFields"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithSimpleFields" to """
                        {
                            "required": [
                                "someBoolList",
                                "someString"
                            ],
                            "type": "object",
                            "properties": {
                                "someBoolList": {
                                    "type": "array",
                                    "items": {
                                      "type": "boolean"
                                    }
                                },
                                "someNullableInt": {
                                    "type": ["integer", "null"],
                                    "format": "int32"
                                },
                                "someString": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<TestEnum>(),
                testName = "enum",
                expectedResultInlining = """
                    {
                        "type": "string",
                        "enum": [ "ONE", "TWO", "THREE" ]
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "type": "string",
                            "enum": [ "ONE", "TWO", "THREE" ]
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.TestEnum"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.TestEnum" to """
                        {
                            "type": "string",
                            "enum": [ "ONE", "TWO", "THREE" ]
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<ClassWithGenericField<String>>(),
                testName = "class with defined generic field",
                expectedResultInlining = """
                    {
                        "required": [
                            "value"
                        ],
                        "type": "object",
                        "properties": {
                            "value": {
                                "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField<kotlin.String>"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField<kotlin.String>" to """
                        {
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<ClassWithGenericField<*>>(),
                testName = "class with wildcard generic field",
                expectedResultInlining = """
                    {
                        "required": [
                            "value"
                        ],
                        "type": "object",
                        "properties": {
                            "value": {
                                "type": "object"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "object"
                                }
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField<*>"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField<*>" to """
                        {
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "object"
                                }
                            }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<ClassWithDeepGeneric<String>>(),
                testName = "class with deep generic field",
                expectedResultInlining = """
                    {
                        "required": [
                            "value"
                        ],
                        "type": "object",
                        "properties": {
                            "value": {
                                "type": "array",
                                "items": {
                                    "type": "string"
                                }
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "array",
                                    "items": {
                                        "type": "string"
                                    }
                                }
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithDeepGeneric<kotlin.String>"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithDeepGeneric<kotlin.String>" to """
                       {
                          "required": [
                              "value"
                          ],
                          "type": "object",
                          "properties": {
                              "value": {
                                  "type": "array",
                                  "items": {
                                      "type": "string"
                                  }
                              }
                          }
                       }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<SealedClass>(),
                testName = "sealed class with subtypes",
                expectedResultInlining = """
                    {
                        "anyOf": [
                            {
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "a": {
                                      "type": "integer",
                                      "format": "int32"
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            },
                            {
                                "required": [
                                    "b",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "format": "int32"
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            }
                        ]
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "anyOf": [
                                {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.SubClassA"
                                },
                                {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.SubClassB"
                                }
                            ]
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.SubClassA" to """
                        {
                            "required": [
                                "a",
                                "sealedValue"
                            ],
                            "type": "object",
                            "properties": {
                                "a": {
                                    "type": "integer",
                                    "format": "int32"
                                },
                                "sealedValue": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.SubClassB" to """
                        {
                            "required": [
                                "b",
                                "sealedValue"
                            ],
                            "type": "object",
                            "properties": {
                                "b": {
                                    "type": "integer",
                                    "format": "int32"
                                },
                                "sealedValue": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.SealedClass"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.SealedClass" to """
                        {
                            "anyOf": [
                                {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.SubClassA"
                                },
                                {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.SubClassB"
                                }
                            ]
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.SubClassA" to """
                        {
                            "required": [
                                "a",
                                "sealedValue"
                            ],
                            "type": "object",
                            "properties": {
                                "a": {
                                    "type": "integer",
                                    "format": "int32"
                                },
                                "sealedValue": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.SubClassB" to """
                        {
                            "required": [
                                "b",
                                "sealedValue"
                            ],
                            "type": "object",
                            "properties": {
                                "b": {
                                    "type": "integer",
                                    "format": "int32"
                                },
                                "sealedValue": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<SubClassA>(),
                testName = "sub class",
                expectedResultInlining = """
                    {
                        "required": [
                            "a",
                            "sealedValue"
                        ],
                        "type": "object",
                        "properties": {
                            "a": {
                                "type": "integer",
                                "format": "int32"
                            },
                            "sealedValue": {
                                "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "required": [
                                "a",
                                "sealedValue"
                            ],
                            "type": "object",
                            "properties": {
                                "a": {
                                    "type": "integer",
                                    "format": "int32"
                                },
                                "sealedValue": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.SubClassA"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.SubClassA" to """
                        {
                            "required": [
                                "a",
                                "sealedValue"
                            ],
                            "type": "object",
                            "properties": {
                                "a": {
                                    "type": "integer",
                                    "format": "int32"
                                },
                                "sealedValue": {
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<CoreAnnotatedClass>(),
                testName = "annotated class (core)",
                withAnnotations = true,
                expectedResultInlining = """
                    {
                        "title": "Annotated Class",
                        "required": [
                            "stringValue",
                            "intValue"
                        ],
                        "type": "object",
                        "properties": {
                            "stringValue": {
                                "type": "string",
                                "description": "String field description",
                                "default": "A default String value",
                                "example": "An example of a String value",
                                "format": "text"
                            },
                            "intValue": {
                                "type": "integer",
                                "format": "int32",
                                "description": "Int field description",
                                "default": "1111",
                                "example": "2222"
                            }
                        },
                        "description": "some description",
                        "deprecated": true,
                        "example": "example 1",
                        "default": "default value",
                        "format": "pair"
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "title": "Annotated Class",
                            "required": [
                                "stringValue",
                                "intValue"
                            ],
                            "type": "object",
                            "properties": {
                                "stringValue": {
                                    "type": "string",
                                    "description": "String field description",
                                    "default": "A default String value",
                                    "example": "An example of a String value",
                                    "format": "text"
                                },
                                "intValue": {
                                    "type": "integer",
                                    "format": "int32",
                                    "description": "Int field description",
                                    "default": "1111",
                                    "example": "2222"
                                }
                            },
                            "description": "some description",
                            "deprecated": true,
                            "example": "example 1",
                            "default": "default value",
                            "format": "pair"
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.CoreAnnotatedClass"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.CoreAnnotatedClass" to """
                        {
                            "title": "Annotated Class",
                            "required": [
                                "stringValue",
                                "intValue"
                            ],
                            "type": "object",
                            "properties": {
                                "stringValue": {
                                    "type": "string",
                                    "description": "String field description",
                                    "default": "A default String value",
                                    "example": "An example of a String value",
                                    "format": "text"
                                },
                                "intValue": {
                                    "type": "integer",
                                    "format": "int32",
                                    "description": "Int field description",
                                    "default": "1111",
                                    "example": "2222"
                                }
                            },
                            "description": "some description",
                            "deprecated": true,
                            "example": "example 1",
                            "default": "default value",
                            "format": "pair"
                        }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<ClassWithNestedClass>(),
                testName = "auto title",
                withAutoTitle = true,
                expectedResultInlining = """
                    {
                        "title": "ClassWithNestedClass",
                        "required": [
                            "nested"
                        ],
                        "type": "object",
                        "properties": {
                            "nested": {
                                "title": "NestedClass",
                                "required": [
                                    "text"
                                ],
                                "type": "object",
                                "properties": {
                                    "text": {
                                        "title": "String",
                                        "type": "string"
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                            "title": "ClassWithNestedClass",
                            "required": [
                                "nested"
                            ],
                            "type": "object",
                            "properties": {
                                "nested": {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.NestedClass"
                                }
                            }
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.NestedClass" to """
                        {
                            "title": "NestedClass",
                            "required": [
                                "text"
                            ],
                            "type": "object",
                            "properties": {
                                "text": {
                                    "title": "String",
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithNestedClass"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithNestedClass" to """
                        {
                            "title": "ClassWithNestedClass",
                            "required": [
                                "nested"
                            ],
                            "type": "object",
                            "properties": {
                                "nested": {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.NestedClass"
                                }
                            }
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.NestedClass" to """
                        {
                            "title": "NestedClass",
                            "required": [
                                "text"
                            ],
                            "type": "object",
                            "properties": {
                                "text": {
                                    "title": "String",
                                    "type": "string"
                                }
                            }
                        }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<ClassWithCollections>(),
                testName = "class with collections",
                expectedResultInlining = """
                    {
                      "required": [
                        "someArray",
                        "someList",
                        "someMap",
                        "someSet"
                      ],
                      "type": "object",
                      "properties": {
                        "someArray": {
                          "type": "array",
                          "items": {
                            "type": "integer",
                            "format": "int32"
                          }
                        },
                        "someList": {
                          "type": "array",
                          "items": {
                            "type": "string"
                          }
                        },
                        "someMap": {
                          "type": "object",
                          "additionalProperties": {
                            "type": "integer",
                            "format": "int32"
                          }
                        },
                        "someSet": {
                          "type": "array",
                          "uniqueItems": true,
                          "items": {
                            "type": "string"
                          }
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                          "required": [
                            "someArray",
                            "someList",
                            "someMap",
                            "someSet"
                          ],
                          "type": "object",
                          "properties": {
                            "someArray": {
                              "type": "array",
                              "items": {
                                "type": "integer",
                                "format": "int32"
                              }
                            },
                            "someList": {
                              "type": "array",
                              "items": {
                                "type": "string"
                              }
                            },
                            "someMap": {
                              "type": "object",
                              "additionalProperties": {
                                "type": "integer",
                                "format": "int32"
                              }
                            },
                            "someSet": {
                              "type": "array",
                              "uniqueItems": true,
                              "items": {
                                "type": "string"
                              }
                            }
                          }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                      {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithCollections"
                      }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithCollections" to """
                        {
                          "required": [
                            "someArray",
                            "someList",
                            "someMap",
                            "someSet"
                          ],
                          "type": "object",
                          "properties": {
                            "someArray": {
                              "type": "array",
                              "items": {
                                "type": "integer",
                                "format": "int32"
                              }
                            },
                            "someList": {
                              "type": "array",
                              "items": {
                                "type": "string"
                              }
                            },
                            "someMap": {
                              "type": "object",
                              "additionalProperties": {
                                "type": "integer",
                                "format": "int32"
                              }
                            },
                            "someSet": {
                              "type": "array",
                              "uniqueItems": true,
                              "items": {
                                "type": "string"
                              }
                            }
                          }
                        }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<ClassDirectSelfReferencing>(),
                testName = "class with direct self reference",
                expectedResultInlining = """
                    {
                      "type": "object",
                      "properties": {
                        "self": {
                          "type": ["object", "null"],
                          "properties": {
                            "self": {
                              "type": ["object", "null"],
                              "properties": {
                                "self": {
                                  "type": ["object", "null"],
                                  "properties": {
                                    "self": {
                                      "type": ["object", "null"],
                                      "properties": {
                                        "self": {
                                          "type": ["object", "null"],
                                          "properties": {
                                            "self": {
                                              "type": ["object", "null"],
                                              "properties": {
                                                "self": {
                                                  "type": ["object", "null"],
                                                  "properties": {
                                                    "self": {
                                                      "type": ["object", "null"],
                                                      "properties": {
                                                        "self": {
                                                          "type": ["object", "null"],
                                                          "properties": {
                                                            "self": {
                                                              "type": ["object", "null"],
                                                              "properties": {
                                                                "self": {
                                                                  "type": ["object", "null"],
                                                                  "properties": {
                                                                    "self": {
                                                                      "type": ["object", "null"],
                                                                      "properties": {
                                                                        "self": {
                                                                          "type": ["object", "null"],
                                                                          "properties": {
                                                                            "self": {
                                                                              "type": ["object", "null"],
                                                                              "properties": {
                                                                                "self": {
                                                                                  "type": ["object", "null"],
                                                                                  "properties": {
                                                                                    "self": {
                                                                                      "type": ["object", "null"],
                                                                                      "properties": {
                                                                                        "self": {
                                                                                          "type": ["object", "null"],
                                                                                          "properties": {
                                                                                            "self": {
                                                                                              "type": ["object", "null"],
                                                                                              "properties": {
                                                                                                "self": {
                                                                                                  "type": ["object", "null"],
                                                                                                  "properties": {
                                                                                                    "self": {
                                                                                                      "type": ["object", "null"],
                                                                                                      "properties": {
                                                                                                        "self": {
                                                                                                          "type": ["object", "null"],
                                                                                                          "properties": {
                                                                                                            "self": {
                                                                                                              "type": ["object", "null"],
                                                                                                              "properties": {
                                                                                                                "self": {
                                                                                                                  "type": ["object", "null"],
                                                                                                                  "properties": {
                                                                                                                    "self": {
                                                                                                                      "type": ["object", "null"],
                                                                                                                      "properties": {
                                                                                                                        "self": {
                                                                                                                          "type": ["object", "null"],
                                                                                                                          "properties": {
                                                                                                                            "self": {
                                                                                                                              "type": ["object", "null"],
                                                                                                                              "properties": {
                                                                                                                                "self": {
                                                                                                                                  "type": ["object", "null"],
                                                                                                                                  "properties": {
                                                                                                                                    "self": {
                                                                                                                                      "type": ["object", "null"],
                                                                                                                                      "properties": {
                                                                                                                                        "self": {
                                                                                                                                          "type": ["object", "null"],
                                                                                                                                          "properties": {
                                                                                                                                            "self": {
                                                                                                                                              "type": ["object", "null"],
                                                                                                                                              "properties": {
                                                                                                                                                "self": {
                                                                                                                                                  "type": ["object", "null"],
                                                                                                                                                  "properties": {
                                                                                                                                                    "self": {
                                                                                                                                                      "type": ["object", "null"],
                                                                                                                                                      "properties": {
                                                                                                                                                        "self": {
                                                                                                                                                        }
                                                                                                                                                      }
                                                                                                                                                    }
                                                                                                                                                  }
                                                                                                                                                }
                                                                                                                                              }
                                                                                                                                            }
                                                                                                                                          }
                                                                                                                                        }
                                                                                                                                      }
                                                                                                                                    }
                                                                                                                                  }
                                                                                                                                }
                                                                                                                              }
                                                                                                                            }
                                                                                                                          }
                                                                                                                        }
                                                                                                                      }
                                                                                                                    }
                                                                                                                  }
                                                                                                                }
                                                                                                              }
                                                                                                            }
                                                                                                          }
                                                                                                        }
                                                                                                      }
                                                                                                    }
                                                                                                  }
                                                                                                }
                                                                                              }
                                                                                            }
                                                                                          }
                                                                                        }
                                                                                      }
                                                                                    }
                                                                                  }
                                                                                }
                                                                              }
                                                                            }
                                                                          }
                                                                        }
                                                                      }
                                                                    }
                                                                  }
                                                                }
                                                              }
                                                            }
                                                          }
                                                        }
                                                      }
                                                    }
                                                  }
                                                }
                                              }
                                            }
                                          }
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                          "type": "object",
                          "properties": {
                            "self": {
                              "oneOf": [
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing"
                                },
                                {
                                  "type": "null"
                                }
                              ]
                            }
                          }
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing" to """
                        {
                          "type": "object",
                          "properties": {
                            "self": {
                              "oneOf": [
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing"
                                },
                                {
                                  "type": "null"
                                }
                              ]
                            }
                          }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                      {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing"
                      }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing" to """
                        {
                          "type": "object",
                          "properties": {
                            "self": {
                              "oneOf": [
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing"
                                },
                                {
                                  "type": "null"
                                }
                              ]
                            }
                          }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<ClassWithOptionalParameters>(),
                testName = "optional parameters as required",
                generatorConfig = {
                    optionalHandling = OptionalHandling.REQUIRED
                },
                expectedResultInlining = """
                    {
                      "required": [
                        "ctorOptional",
                        "ctorRequired"
                      ],
                      "type": "object",
                      "properties": {
                        "ctorOptional": {
                          "type": "string"
                        },
                        "ctorOptionalNullable": {
                          "type": ["string", "null"]
                        },
                        "ctorRequired": {
                          "type": "string"
                        },
                        "ctorRequiredNullable": {
                          "type": ["string", "null"]
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                          "required": [
                            "ctorOptional",
                            "ctorRequired"
                          ],
                          "type": "object",
                          "properties": {
                            "ctorOptional": {
                              "type": "string"
                            },
                            "ctorOptionalNullable": {
                              "type": ["string", "null"]
                            },
                            "ctorRequired": {
                              "type": "string"
                            },
                            "ctorRequiredNullable": {
                              "type": ["string", "null"]
                            }
                          }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                          "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters" to """
                        {
                          "required": [
                            "ctorOptional",
                            "ctorRequired"
                          ],
                          "type": "object",
                          "properties": {
                            "ctorOptional": {
                              "type": "string"
                            },
                            "ctorOptionalNullable": {
                              "type": ["string", "null"]
                            },
                            "ctorRequired": {
                              "type": "string"
                            },
                            "ctorRequiredNullable": {
                              "type": ["string", "null"]
                            }
                          }
                        }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<ClassWithOptionalParameters>(),
                testName = "optional parameters as non-required",
                generatorConfig = {
                    optionalHandling = OptionalHandling.NON_REQUIRED
                },
                expectedResultInlining = """
                    {
                      "required": [
                        "ctorRequired"
                      ],
                      "type": "object",
                      "properties": {
                        "ctorOptional": {
                          "type": "string"
                        },
                        "ctorOptionalNullable": {
                          "type": ["string", "null"]
                        },
                        "ctorRequired": {
                          "type": "string"
                        },
                        "ctorRequiredNullable": {
                          "type": ["string", "null"]
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                          "required": [
                            "ctorRequired"
                          ],
                          "type": "object",
                          "properties": {
                            "ctorOptional": {
                              "type": "string"
                            },
                            "ctorOptionalNullable": {
                              "type": ["string", "null"]
                            },
                            "ctorRequired": {
                              "type": "string"
                            },
                            "ctorRequiredNullable": {
                              "type": ["string", "null"]
                            }
                          }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                          "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters" to """
                        {
                          "required": [
                            "ctorRequired"
                          ],
                          "type": "object",
                          "properties": {
                            "ctorOptional": {
                              "type": "string"
                            },
                            "ctorOptionalNullable": {
                              "type": ["string", "null"]
                            },
                            "ctorRequired": {
                              "type": "string"
                            },
                            "ctorRequiredNullable": {
                              "type": ["string", "null"]
                            }
                          }
                        }
                    """.trimIndent()
                )
            ),
            TestData(
                type = typeOf<ClassWithValueClass>(),
                testName = "inline value class",
                expectedResultInlining = """
                    {
                      "required": [
                        "myValue",
                        "someText"
                      ],
                      "type": "object",
                      "properties": {
                        "myValue": {
                          "type": "integer",
                          "format": "int32"
                        },
                        "someText": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                          "required": [
                            "myValue",
                            "someText"
                          ],
                          "type": "object",
                          "properties": {
                            "myValue": {
                              "type": "integer",
                              "format": "int32"
                            },
                            "someText": {
                              "type": "string"
                            }
                          }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                        {
                          "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithValueClass"
                        }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithValueClass" to """
                        {
                          "required": [
                            "myValue",
                            "someText"
                          ],
                          "type": "object",
                          "properties": {
                            "myValue": {
                              "type": "integer",
                              "format": "int32"
                            },
                            "someText": {
                              "type": "string"
                            }
                          }
                        }
                    """.trimIndent()
                ),
            ),
            TestData(
                type = typeOf<ClassWithAnnotatedValueClass>(),
                testName = "inline annotated value class",
                withAnnotations = true,
                expectedResultInlining = """
                    {
                      "required": [
                        "myValue"
                      ],
                      "type": "object",
                      "properties": {
                        "myValue": {
                          "maximum": 15,
                          "minimum": 5,
                          "type": "string",
                          "description": "annotated value class for testing.",
                          "default": "default on property"
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = mapOf(
                    "." to """
                        {
                          "required": [
                            "myValue"
                          ],
                          "type": "object",
                          "properties": {
                            "myValue": {
                              "maximum": 15,
                              "minimum": 5,
                              "type": "string",
                              "description": "annotated value class for testing.",
                              "default": "default on property"
                            }
                          }
                        }
                    """.trimIndent()
                ),
                expectedResultReferencingRoot = mapOf(
                    "." to """
                      {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithAnnotatedValueClass"
                      }
                    """.trimIndent(),
                    "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithAnnotatedValueClass" to """
                        {
                          "required": [
                            "myValue"
                          ],
                          "type": "object",
                          "properties": {
                            "myValue": {
                              "maximum": 15,
                              "minimum": 5,
                              "type": "string",
                              "description": "annotated value class for testing.",
                              "default": "default on property"
                            }
                          }
                        }
                    """.trimIndent()
                ),
            ),
        )
    }
}