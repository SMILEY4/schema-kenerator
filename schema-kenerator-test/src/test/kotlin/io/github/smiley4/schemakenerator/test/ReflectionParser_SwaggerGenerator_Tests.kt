package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAutoTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceRootStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDefaultStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDeprecatedStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDescriptionStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationExamplesStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaGenerationStep
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithCollections
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithDeepGeneric
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithNestedClass
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithSimpleFields
import io.github.smiley4.schemakenerator.test.models.reflection.CoreAnnotatedClass
import io.github.smiley4.schemakenerator.test.models.reflection.SealedClass
import io.github.smiley4.schemakenerator.test.models.reflection.SubClassA
import io.github.smiley4.schemakenerator.test.models.reflection.TestEnum
import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson
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
                .let { ReflectionTypeProcessingStep().process(it) }
                .let { SwaggerSchemaGenerationStep().generate(it) }
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .let { SwaggerSchemaCoreAnnotationTitleStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDescriptionStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDefaultStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationExamplesStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDeprecatedStep().process(it) }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { SwaggerSchemaAutoTitleStep(TitleType.SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { SwaggerSchemaCompileInlineStep().compile(it) }
                .let {
                    Result(
                        schema = it.swagger,
                        definitions = emptyMap()
                    )
                }

            json.writeValueAsString(schema).shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedResultInlining
            }
        }
    }

    context("generator: referencing") {
        withData(TEST_DATA) { data ->

            val schema = data.type
                .let { ReflectionTypeProcessingStep().process(it) }
                .let { SwaggerSchemaGenerationStep().generate(it) }
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .let { SwaggerSchemaCoreAnnotationTitleStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDescriptionStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDefaultStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationExamplesStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDeprecatedStep().process(it) }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { SwaggerSchemaAutoTitleStep(TitleType.SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { SwaggerSchemaCompileReferenceStep().compile(it) }
                .let {
                    Result(
                        schema = it.swagger,
                        definitions = it.componentSchemas
                    )
                }

            json.writeValueAsString(schema).shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedResultReferencing
            }
        }
    }

    context("generator: referencing-root") {
        withData(TEST_DATA) { data ->

            val schema = data.type
                .let { ReflectionTypeProcessingStep().process(it) }
                .let { SwaggerSchemaGenerationStep().generate(it) }
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .let { SwaggerSchemaCoreAnnotationTitleStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDescriptionStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDefaultStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationExamplesStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDeprecatedStep().process(it) }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { SwaggerSchemaAutoTitleStep(TitleType.SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { SwaggerSchemaCompileReferenceRootStep().compile(it) }
                .let {
                    Result(
                        schema = it.swagger,
                        definitions = it.componentSchemas
                    )
                }

            json.writeValueAsString(schema).shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedResultReferencingRoot
            }
        }
    }

}) {

    companion object {

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!

        private data class Result(
            val schema: Schema<*>,
            val definitions: Map<String, Schema<*>>
        )

        private class TestData(
            val testName: String,
            val type: KType,
            val withAnnotations: Boolean = false,
            val withAutoTitle: Boolean = false,
            val expectedResultInlining: String,
            val expectedResultReferencing: String,
            val expectedResultReferencingRoot: String,
        ) : WithDataTestName {
            override fun dataTestName() = testName
        }

        private val TEST_DATA = listOf(
            TestData(
                type = typeOf<Any>(),
                testName = "any",
                expectedResultInlining = """
                    {
                        "schema": {
                            "type": "object",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "object",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "object",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<UByte>(),
                testName = "ubyte",
                expectedResultInlining = """
                    {
                        "schema": {
                            "type": "integer",
                            "maximum": 255,
                            "minimum": 0,
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "integer",
                            "maximum": 255,
                            "minimum": 0,
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "integer",
                            "maximum": 255,
                            "minimum": 0,
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<Int>(),
                testName = "int",
                expectedResultInlining = """
                    {
                        "schema": {
                            "type": "integer",
                            "format": "int32",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "integer",
                            "format": "int32",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "integer",
                            "format": "int32",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent()
            ),
            TestData(
                type = typeOf<Float>(),
                testName = "float",
                expectedResultInlining = """
                    {
                        "schema": {
                            "type": "number",
                            "format": "float",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "number",
                            "format": "float",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "number",
                            "format": "float",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<Boolean>(),
                testName = "boolean",
                expectedResultInlining = """
                    {
                        "schema": {
                            "type": "boolean",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "boolean",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "boolean",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<String>(),
                testName = "string",
                expectedResultInlining = """
                    {
                        "schema": {
                            "type": "string",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "string",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "string",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<List<String>>(),
                testName = "list of strings",
                expectedResultInlining = """
                    {
                        "schema": {
                            "type": "array",
                            "exampleSetFlag": false,
                            "items": {
                                "type": "string",
                                "exampleSetFlag": false
                            }
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "array",
                            "exampleSetFlag": false,
                            "items": {
                                "type": "string",
                                "exampleSetFlag": false
                            }
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "array",
                            "exampleSetFlag": false,
                            "items": {
                                "type": "string",
                                "exampleSetFlag": false
                            }
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<Map<String, Int>>(),
                testName = "map of strings to integers",
                expectedResultInlining = """
                    {
                        "schema": {
                            "type": "object",
                            "additionalProperties": {
                                "type": "integer",
                                "format": "int32",
                                "exampleSetFlag": false
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "type": "object",
                            "additionalProperties": {
                                "type": "integer",
                                "format": "int32",
                                "exampleSetFlag": false
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "type": "object",
                            "additionalProperties": {
                                "type": "integer",
                                "format": "int32",
                                "exampleSetFlag": false
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithSimpleFields>(),
                testName = "class with simple fields",
                expectedResultInlining = """
                    {
                        "schema": {
                            "required": [
                                "someBoolList",
                                "someString"
                            ],
                            "type": "object",
                            "properties": {
                                "someBoolList": {
                                    "type": "array",
                                    "exampleSetFlag": false,
                                    "items": {
                                      "type": "boolean",
                                      "exampleSetFlag": false
                                    }
                                },
                                "someNullableInt": {
                                    "type": "integer",
                                    "format": "int32",
                                    "exampleSetFlag": false
                                },
                                "someString": {
                                    "type": "string",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "required": [
                                "someBoolList",
                                "someString"
                            ],
                            "type": "object",
                            "properties": {
                                "someBoolList": {
                                    "type": "array",
                                    "exampleSetFlag": false,
                                    "items": {
                                      "type": "boolean",
                                      "exampleSetFlag": false
                                    }
                                },
                                "someNullableInt": {
                                    "type": "integer",
                                    "format": "int32",
                                    "exampleSetFlag": false
                                },
                                "someString": {
                                    "type": "string",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithSimpleFields",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithSimpleFields": {
                                "required": [
                                    "someBoolList",
                                    "someString"
                                ],
                                "type": "object",
                                "properties": {
                                    "someBoolList": {
                                        "type": "array",
                                        "exampleSetFlag": false,
                                        "items": {
                                            "type": "boolean",
                                            "exampleSetFlag": false
                                        }
                                    },
                                    "someNullableInt": {
                                        "type": "integer",
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "someString": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<TestEnum>(),
                testName = "enum",
                expectedResultInlining = """
                    {
                        "schema": {
                            "exampleSetFlag": false,
                            "enum": [ "ONE", "TWO", "THREE" ]
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "exampleSetFlag": false,
                            "enum": [ "ONE", "TWO", "THREE" ]
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.TestEnum",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.TestEnum": {
                                "exampleSetFlag": false,
                                "enum": [ "ONE", "TWO", "THREE" ]
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithGenericField<String>>(),
                testName = "class with defined generic field",
                expectedResultInlining = """
                    {
                        "schema": {
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "string",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "string",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField<kotlin.String>",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField<kotlin.String>": {
                                "required": [
                                    "value"
                                ],
                                "type": "object",
                                "properties": {
                                    "value": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithGenericField<*>>(),
                testName = "class with wildcard generic field",
                expectedResultInlining = """
                    {
                        "schema": {
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "object",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "object",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField<*>",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField<*>": {
                                "required": [
                                    "value"
                                ],
                                "type": "object",
                                "properties": {
                                    "value": {
                                        "type": "object",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithDeepGeneric<String>>(),
                testName = "class with deep generic field",
                expectedResultInlining = """
                    {
                        "schema": {
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "array",
                                    "exampleSetFlag": false,
                                    "items": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "array",
                                    "exampleSetFlag": false,
                                    "items": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithDeepGeneric<kotlin.String>",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithDeepGeneric<kotlin.String>": {
                                "required": [
                                    "value"
                                ],
                                "type": "object",
                                "properties": {
                                    "value": {
                                        "type": "array",
                                        "exampleSetFlag": false,
                                        "items": {
                                            "type": "string",
                                            "exampleSetFlag": false
                                        }
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<SealedClass>(),
                testName = "sealed class with subtypes",
                expectedResultInlining = """
                    {
                        "schema": {
                            "exampleSetFlag": false,
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
                                              "format": "int32",
                                              "exampleSetFlag": false
                                            },
                                        "sealedValue": {
                                            "type": "string",
                                            "exampleSetFlag": false
                                        }
                                    },
                                    "exampleSetFlag": false
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
                                            "format": "int32",
                                            "exampleSetFlag": false
                                        },
                                        "sealedValue": {
                                            "type": "string",
                                            "exampleSetFlag": false
                                        }
                                    },
                                    "exampleSetFlag": false
                                }
                            ]
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "exampleSetFlag": false,
                            "anyOf": [
                                {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.SubClassA",
                                    "exampleSetFlag": false
                                },
                                {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.SubClassB",
                                    "exampleSetFlag": false
                                }
                            ]
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.SubClassA": {
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            },
                            "io.github.smiley4.schemakenerator.test.models.reflection.SubClassB": {
                                "required": [
                                    "b",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.SealedClass",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.SealedClass": {
                                "exampleSetFlag": false,
                                "anyOf": [
                                    {
                                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.SubClassA",
                                        "exampleSetFlag": false
                                    },
                                    {
                                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.SubClassB",
                                        "exampleSetFlag": false
                                    }
                                ]
                            },
                            "io.github.smiley4.schemakenerator.test.models.reflection.SubClassA": {
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            },
                            "io.github.smiley4.schemakenerator.test.models.reflection.SubClassB": {
                                "required": [
                                    "b",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<SubClassA>(),
                testName = "sub class",
                expectedResultInlining = """
                    {
                        "schema": {
                            "required": [
                                "a",
                                "sealedValue"
                            ],
                            "type": "object",
                            "properties": {
                                "a": {
                                    "type": "integer",
                                    "format": "int32",
                                    "exampleSetFlag": false
                                },
                                "sealedValue": {
                                    "type": "string",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "required": [
                                "a",
                                "sealedValue"
                            ],
                            "type": "object",
                            "properties": {
                                "a": {
                                    "type": "integer",
                                    "format": "int32",
                                    "exampleSetFlag": false
                                },
                                "sealedValue": {
                                    "type": "string",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.SubClassA",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.SubClassA": {
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "type": "object",
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<CoreAnnotatedClass>(),
                testName = "annotated class (core)",
                withAnnotations = true,
                expectedResultInlining = """
                    {
                        "schema": {
                            "title": "Annotated Class",
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "string",
                                    "description": "field description",
                                    "exampleSetFlag": false
                                }
                            },
                            "description": "some description",
                            "deprecated": true,
                            "exampleSetFlag": false,
                            "examples": [
                                "example 1",
                                "example 2"
                            ],
                            "default": "default value"
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "title": "Annotated Class",
                            "required": [
                                "value"
                            ],
                            "type": "object",
                            "properties": {
                                "value": {
                                    "type": "string",
                                    "description": "field description",
                                    "exampleSetFlag": false
                                }
                            },
                            "description": "some description",
                            "deprecated": true,
                            "exampleSetFlag": false,
                            "examples": [
                                "example 1",
                                "example 2"
                            ],
                            "default": "default value"
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.CoreAnnotatedClass",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.CoreAnnotatedClass": {
                                "title": "Annotated Class",
                                "required": [
                                    "value"
                                ],
                                "type": "object",
                                "properties": {
                                    "value": {
                                        "type": "string",
                                        "description": "field description",
                                        "exampleSetFlag": false
                                    }
                                },
                                "description": "some description",
                                "deprecated": true,
                                "exampleSetFlag": false,
                                "examples": [
                                    "example 1",
                                    "example 2"
                                ],
                                "default": "default value"
                            }
                        }
                    }
                """.trimIndent(),
            ),
//            TestData(
//                type = typeOf<ClassWithLocalDateTime>(),
//                testName = "class with java local-date-time and custom parser",
//                customParsers = mapOf(
//                    LocalDateTime::class to CustomReflectionTypeParser { typeId, _ ->
//                        PrimitiveTypeData(
//                            id = typeId,
//                            simpleName = String::class.simpleName!!,
//                            qualifiedName = String::class.qualifiedName!!,
//                            typeParameters = mutableMapOf()
//                        )
//                    }
//                ),
//                expectedResultInlining = """
//                    {
//                        "schema": {
//                            "required": [
//                                "dateTime"
//                            ],
//                            "type": "object",
//                            "properties": {
//                                "dateTime": {
//                                    "type": "string",
//                                    "exampleSetFlag": false
//                                }
//                            },
//                            "exampleSetFlag": false
//                        },
//                        "definitions": {}
//                    }
//                """.trimIndent(),
//                expectedResultReferencing = """
//                    {
//                        "schema": {
//                            "required": [
//                                "dateTime"
//                            ],
//                            "type": "object",
//                            "properties": {
//                                "dateTime": {
//                                    "type": "string",
//                                    "exampleSetFlag": false
//                                }
//                            },
//                            "exampleSetFlag": false
//                        },
//                        "definitions": {}
//                    }
//                """.trimIndent(),
//                expectedResultReferencingRoot = """
//                    {
//                        "schema": {
//                            "${'$'}ref": "#/definitions/ClassWithLocalDateTime",
//                            "exampleSetFlag": false
//                        },
//                        "definitions": {
//                            "ClassWithLocalDateTime": {
//                                "required": [
//                                    "dateTime"
//                                ],
//                                "type": "object",
//                                "properties": {
//                                    "dateTime": {
//                                        "type": "string",
//                                        "exampleSetFlag": false
//                                    }
//                                },
//                                "exampleSetFlag": false
//                            }
//                        }
//                    }
//                """.trimIndent(),
//            ),
            TestData(
                type = typeOf<ClassWithNestedClass>(),
                testName = "auto title",
                withAutoTitle = true,
                expectedResultInlining = """
                    {
                        "schema": {
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
                                            "type": "string",
                                            "exampleSetFlag": false
                                        }
                                    },
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "title": "ClassWithNestedClass",
                            "required": [
                                "nested"
                            ],
                            "type": "object",
                            "properties": {
                                "nested": {
                                    "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.NestedClass",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.NestedClass": {
                                "title": "NestedClass",
                                "required": [
                                    "text"
                                ],
                                "type": "object",
                                "properties": {
                                    "text": {
                                        "title": "String",
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithNestedClass",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.NestedClass": {
                                "title": "NestedClass",
                                "required": [
                                    "text"
                                ],
                                "type": "object",
                                "properties": {
                                    "text": {
                                        "title": "String",
                                        "type": "string",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            },
                            "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithNestedClass": {
                                "title": "ClassWithNestedClass",
                                "required": [
                                    "nested"
                                ],
                                "type": "object",
                                "properties": {
                                    "nested": {
                                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.NestedClass",
                                        "exampleSetFlag": false
                                    }
                                },
                                "exampleSetFlag": false
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithCollections>(),
                testName = "class with collections",
                expectedResultInlining = """
                    {
                      "schema": {
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
                            "exampleSetFlag": false,
                            "items": {
                              "type": "integer",
                              "format": "int32",
                              "exampleSetFlag": false
                            }
                          },
                          "someList": {
                            "type": "array",
                            "exampleSetFlag": false,
                            "items": {
                              "type": "string",
                              "exampleSetFlag": false
                            }
                          },
                          "someMap": {
                            "type": "object",
                            "additionalProperties": {
                              "type": "integer",
                              "format": "int32",
                              "exampleSetFlag": false
                            },
                            "exampleSetFlag": false
                          },
                          "someSet": {
                            "type": "array",
                            "uniqueItems": true,
                            "exampleSetFlag": false,
                            "items": {
                              "type": "string",
                              "exampleSetFlag": false
                            }
                          }
                        },
                        "exampleSetFlag": false
                      },
                      "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "schema": {
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
                            "exampleSetFlag": false,
                            "items": {
                              "type": "integer",
                              "format": "int32",
                              "exampleSetFlag": false
                            }
                          },
                          "someList": {
                            "type": "array",
                            "exampleSetFlag": false,
                            "items": {
                              "type": "string",
                              "exampleSetFlag": false
                            }
                          },
                          "someMap": {
                            "type": "object",
                            "additionalProperties": {
                              "type": "integer",
                              "format": "int32",
                              "exampleSetFlag": false
                            },
                            "exampleSetFlag": false
                          },
                          "someSet": {
                            "type": "array",
                            "uniqueItems": true,
                            "exampleSetFlag": false,
                            "items": {
                              "type": "string",
                              "exampleSetFlag": false
                            }
                          }
                        },
                        "exampleSetFlag": false
                      },
                      "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                      "schema": {
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithCollections",
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithCollections": {
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
                              "exampleSetFlag": false,
                              "items": {
                                "type": "integer",
                                "format": "int32",
                                "exampleSetFlag": false
                              }
                            },
                            "someList": {
                              "type": "array",
                              "exampleSetFlag": false,
                              "items": {
                                "type": "string",
                                "exampleSetFlag": false
                              }
                            },
                            "someMap": {
                              "type": "object",
                              "additionalProperties": {
                                "type": "integer",
                                "format": "int32",
                                "exampleSetFlag": false
                              },
                              "exampleSetFlag": false
                            },
                            "someSet": {
                              "type": "array",
                              "uniqueItems": true,
                              "exampleSetFlag": false,
                              "items": {
                                "type": "string",
                                "exampleSetFlag": false
                              }
                            }
                          },
                          "exampleSetFlag": false
                        }
                      }
                    }
                """.trimIndent(),
            ),
        )

    }

}