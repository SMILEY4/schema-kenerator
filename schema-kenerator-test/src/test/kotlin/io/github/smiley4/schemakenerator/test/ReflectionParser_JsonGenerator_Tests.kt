package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerationStepConfig
import io.github.smiley4.schemakenerator.jsonschema.OptionalHandling
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.obj
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileReferenceRootStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileReferenceStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationDefaultStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationDeprecatedStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationDescriptionStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationExamplesStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationFormatStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCoreAnnotationTitleStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaGenerationStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaTitleStep
import io.github.smiley4.schemakenerator.jsonschema.steps.TitleBuilder
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep
import io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing
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
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("ClassName")
class ReflectionParser_JsonGenerator_Tests : FunSpec({

    context("generator: inlining") {
        withData(TEST_DATA) { data ->

            val schema = data.type
                .let { ReflectionTypeProcessingStep().process(it) }
                .let {
                    JsonSchemaGenerationStep(
                        optionalAsNonRequired = JsonSchemaGenerationStepConfig().apply(data.generatorConfig).optionalHandling == OptionalHandling.NON_REQUIRED
                    ).generate(it)
                }
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .let { JsonSchemaCoreAnnotationTitleStep().process(it) }
                            .let { JsonSchemaCoreAnnotationDescriptionStep().process(it) }
                            .let { JsonSchemaCoreAnnotationDefaultStep().process(it) }
                            .let { JsonSchemaCoreAnnotationExamplesStep().process(it) }
                            .let { JsonSchemaCoreAnnotationDeprecatedStep().process(it) }
                            .let { JsonSchemaCoreAnnotationFormatStep().process(it) }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { JsonSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { JsonSchemaCompileInlineStep().compile(it) }

            schema.json.shouldEqualJson(data.expectedResultInlining)
        }
    }

    context("generator: referencing") {
        withData(TEST_DATA) { data ->

            val schema = data.type
                .let { ReflectionTypeProcessingStep().process(it) }
                .let {
                    JsonSchemaGenerationStep(
                        optionalAsNonRequired = JsonSchemaGenerationStepConfig().apply(data.generatorConfig).optionalHandling == OptionalHandling.NON_REQUIRED
                    ).generate(it)
                }
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .let { JsonSchemaCoreAnnotationTitleStep().process(it) }
                            .let { JsonSchemaCoreAnnotationDescriptionStep().process(it) }
                            .let { JsonSchemaCoreAnnotationDefaultStep().process(it) }
                            .let { JsonSchemaCoreAnnotationExamplesStep().process(it) }
                            .let { JsonSchemaCoreAnnotationDeprecatedStep().process(it) }
                            .let { JsonSchemaCoreAnnotationFormatStep().process(it) }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { JsonSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { JsonSchemaCompileReferenceStep(TitleBuilder.BUILDER_FULL).compile(it) }
                .also {
                    if (it.definitions.isNotEmpty()) {
                        (it.json as JsonObject).properties["definitions"] = obj {
                            it.definitions.forEach { (k, v) ->
                                k to v
                            }
                        }
                    }
                }

            schema.json.shouldEqualJson(data.expectedResultReferencing)

        }
    }

    context("generator: referencing-root") {
        withData(TEST_DATA) { data ->

            val schema = data.type
                .let { ReflectionTypeProcessingStep().process(it) }
                .let {
                    JsonSchemaGenerationStep(
                        optionalAsNonRequired = JsonSchemaGenerationStepConfig().apply(data.generatorConfig).optionalHandling == OptionalHandling.NON_REQUIRED
                    ).generate(it)
                }
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .let { JsonSchemaCoreAnnotationTitleStep().process(it) }
                            .let { JsonSchemaCoreAnnotationDescriptionStep().process(it) }
                            .let { JsonSchemaCoreAnnotationDefaultStep().process(it) }
                            .let { JsonSchemaCoreAnnotationExamplesStep().process(it) }
                            .let { JsonSchemaCoreAnnotationDeprecatedStep().process(it) }
                            .let { JsonSchemaCoreAnnotationFormatStep().process(it) }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { JsonSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { JsonSchemaCompileReferenceRootStep(TitleBuilder.BUILDER_FULL).compile(it) }
                .also {
                    if (it.definitions.isNotEmpty()) {
                        (it.json as JsonObject).properties["definitions"] = obj {
                            it.definitions.forEach { (k, v) ->
                                k to v
                            }
                        }
                    }
                }

            schema.json.shouldEqualJson(data.expectedResultReferencingRoot)
        }
    }

}) {

    companion object {

        private class TestData(
            val testName: String,
            val type: KType,
            val generatorConfig: JsonSchemaGenerationStepConfig.() -> Unit = {},
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
                        "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "object"
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<UByte>(),
                testName = "ubyte",
                expectedResultInlining = """
                    {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 255
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 255
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 255
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<Int>(),
                testName = "int",
                expectedResultInlining = """
                    {
                        "type": "integer",
                        "minimum": -2147483648,
                        "maximum": 2147483647
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "integer",
                        "minimum": -2147483648,
                        "maximum": 2147483647
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "integer",
                        "minimum": -2147483648,
                        "maximum": 2147483647
                    }
                """.trimIndent()
            ),
            TestData(
                type = typeOf<Float>(),
                testName = "float",
                expectedResultInlining = """
                    {
                        "type": "number",
                        "minimum": 1.4E-45,
                        "maximum": 3.4028235E38
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "number",
                        "minimum": 1.4E-45,
                        "maximum": 3.4028235E38
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "number",
                        "minimum": 1.4E-45,
                        "maximum": 3.4028235E38
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<Boolean>(),
                testName = "boolean",
                expectedResultInlining = """
                    {
                        "type": "boolean"
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "boolean"
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "boolean"
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<String>(),
                testName = "string",
                expectedResultInlining = """
                    {
                        "type": "string"
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "string"
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "string"
                    }
                """.trimIndent(),
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
                expectedResultReferencing = """
                    {
                        "type": "array",
                        "items": {
                            "type": "string"
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "array",
                        "items": {
                            "type": "string"
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<Map<String, Int>>(),
                testName = "map of strings to integers",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "additionalProperties": {
                            "type": "integer",
                            "minimum": -2147483648,
                            "maximum": 2147483647
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "additionalProperties": {
                            "type": "integer",
                            "minimum": -2147483648,
                            "maximum": 2147483647
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "type": "object",
                        "additionalProperties": {
                            "type": "integer",
                            "minimum": -2147483648,
                            "maximum": 2147483647
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithSimpleFields>(),
                testName = "class with simple fields",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "required": ["someString", "someBoolList"],
                        "properties": {
                            "someString": {
                                "type": "string"
                            },
                            "someNullableInt": {
                                "type": "integer",
                                "minimum": -2147483648,
                                "maximum": 2147483647
                            },
                            "someBoolList": {
                                "type": "array",
                                "items": {
                                    "type": "boolean"
                                }
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": ["someString", "someBoolList"],
                        "properties": {
                            "someString": {
                                "type": "string"
                            },
                            "someNullableInt": {
                                "type": "integer",
                                "minimum": -2147483648,
                                "maximum": 2147483647
                            },
                            "someBoolList": {
                                "type": "array",
                                "items": {
                                    "type": "boolean"
                                }
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithSimpleFields",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithSimpleFields": {
                                "type": "object",
                                "required": [
                                    "someBoolList",
                                    "someString"
                                ],
                                "properties": {
                                    "someBoolList": {
                                        "type": "array",
                                        "items": {
                                            "type": "boolean"
                                        }
                                    },
                                    "someNullableInt": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "someString": {
                                        "type": "string"
                                    }
                                }
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
                        "enum": [ "ONE", "TWO", "THREE"]
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "enum": [ "ONE", "TWO", "THREE"]
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                {
                    "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.TestEnum",
                    "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.TestEnum": {
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
                        "type": "object",
                        "required": ["value"],
                        "properties": {
                            "value": {
                                "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": ["value"],
                        "properties": {
                            "value": {
                                "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                {
                    "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField<kotlin.String>",
                    "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField<kotlin.String>": {
                            "type": "object",
                            "required": [
                                "value"
                            ],
                            "properties": {
                                "value": {
                                    "type": "string"
                                }
                            }
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
                        "type": "object",
                        "required": [ "value" ],
                        "properties": {
                            "value": {
                                "type": "object"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": [ "value" ],
                        "properties": {
                            "value": {
                                "type": "object"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField<*>",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField<*>": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "object"
                                    }
                                }
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
                        "type": "object",
                        "required": ["value"],
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
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": ["value"],
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
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithDeepGeneric<kotlin.String>",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithDeepGeneric<kotlin.String>": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "array",
                                        "items": {
                                            "type": "string"
                                        }
                                    }
                                }
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
                        "anyOf": [
                            {
                                "type": "object",
                                "required": ["a","sealedValue"],
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            },
                            {
                                "type": "object",
                                "required": ["b", "sealedValue"],
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            }
                        ]
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "anyOf": [
                            {
                                "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.SubClassA"
                            },
                            {
                                "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.SubClassB"
                            }
                        ],
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.SubClassA": {
                                "type": "object",
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            },
                            "io.github.smiley4.schemakenerator.test.models.reflection.SubClassB": {
                                "type": "object",
                                "required": [
                                    "b",
                                    "sealedValue"
                                ],
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.SealedClass",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.SealedClass": {
                                "anyOf": [
                                    {
                                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.SubClassA"
                                    },
                                    {
                                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.SubClassB"
                                    }
                                ]
                            },
                            "io.github.smiley4.schemakenerator.test.models.reflection.SubClassA": {
                                "type": "object",
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
                            },
                            "io.github.smiley4.schemakenerator.test.models.reflection.SubClassB": {
                                "type": "object",
                                "required": [
                                    "b",
                                    "sealedValue"
                                ],
                                "properties": {
                                    "b": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
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
                        "type": "object",
                        "required": ["a","sealedValue"],
                        "properties": {
                            "a": {
                                "type": "integer",
                                "minimum": -2147483648,
                                "maximum": 2147483647
                            },
                            "sealedValue": {
                                "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": [
                            "a",
                            "sealedValue"
                        ],
                        "properties": {
                            "a": {
                                "type": "integer",
                                "minimum": -2147483648,
                                "maximum": 2147483647
                            },
                            "sealedValue": {
                                "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.SubClassA",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.SubClassA": {
                                "type": "object",
                                "required": [
                                    "a",
                                    "sealedValue"
                                ],
                                "properties": {
                                    "a": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    },
                                    "sealedValue": {
                                        "type": "string"
                                    }
                                }
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
                        "type": "object",
                        "required": ["stringValue", "intValue"],
                        "properties": {
                            "stringValue": {
                                "type": "string",
                                "description": "String field description",
                                "default": "A default String value",
                                "examples": [
                                    "An example of a String value"
                                ],
                                "format": "text"
                            },
                            "intValue": {
                                "type": "integer",
                                "default": "1111",
                                "minimum": -2147483648,
                                "maximum": 2147483647,
                                "description": "Int field description",
                                "examples": [
                                    "2222"
                                ],
                                "format": "int32"
                            }
                        },
                        "title": "Annotated Class",
                        "description": "some description",
                        "default": "default value",
                        "examples": [
                            "example 1"
                        ],
                        "deprecated": true,
                        "format": "pair"
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": ["stringValue", "intValue"],
                        "properties": {
                            "stringValue": {
                                "type": "string",
                                "description": "String field description",
                                "default": "A default String value",
                                "examples": [
                                    "An example of a String value"
                                ],
                                "format": "text"
                            },
                            "intValue": {
                                "type": "integer",
                                "default": "1111",
                                "minimum": -2147483648,
                                "maximum": 2147483647,
                                "description": "Int field description",
                                "examples": [
                                    "2222"
                                ],
                                "format": "int32"
                            }
                        },
                        "title": "Annotated Class",
                        "description": "some description",
                        "default": "default value",
                        "examples": [
                            "example 1"
                        ],
                        "deprecated": true,
                        "format": "pair"
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.CoreAnnotatedClass",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.CoreAnnotatedClass": {
                                "type": "object",
                                "required": ["stringValue", "intValue"],
                                "properties": {
                                    "stringValue": {
                                        "type": "string",
                                        "description": "String field description",
                                        "default": "A default String value",
                                        "examples": [
                                            "An example of a String value"
                                        ],
                                        "format": "text"
                                    },
                                    "intValue": {
                                        "type": "integer",
                                        "default": "1111",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647,
                                        "description": "Int field description",
                                        "examples": [
                                            "2222"
                                        ],
                                        "format": "int32"
                                    }
                                },
                                "title": "Annotated Class",
                                "description": "some description",
                                "default": "default value",
                                "examples": [
                                    "example 1"
                                ],
                                "deprecated": true,
                                "format": "pair"
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithNestedClass>(),
                testName = "auto title",
                withAutoTitle = true,
                expectedResultInlining = """
                    {
                        "type": "object",
                        "required": [
                            "nested"
                        ],
                        "properties": {
                            "nested": {
                                "type": "object",
                                "required": [
                                    "text"
                                ],
                                "properties": {
                                    "text": {
                                        "type": "string",
                                        "title": "String"
                                    }
                                },
                                "title": "NestedClass"
                            }
                        },
                        "title": "ClassWithNestedClass"
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": [
                            "nested"
                        ],
                        "properties": {
                            "nested": {
                                "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.NestedClass"
                            }
                        },
                        "title": "ClassWithNestedClass",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.NestedClass": {
                                "type": "object",
                                "required": [
                                    "text"
                                ],
                                "properties": {
                                    "text": {
                                        "type": "string",
                                        "title": "String"
                                    }
                                },
                                "title": "NestedClass"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithNestedClass",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.reflection.NestedClass": {
                                "type": "object",
                                "required": [
                                    "text"
                                ],
                                "properties": {
                                    "text": {
                                        "type": "string",
                                        "title": "String"
                                    }
                                },
                                "title": "NestedClass"
                            },
                            "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithNestedClass": {
                                "type": "object",
                                "required": [
                                    "nested"
                                ],
                                "properties": {
                                    "nested": {
                                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.NestedClass"
                                    }
                                },
                                "title": "ClassWithNestedClass"
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
                      "type": "object",
                      "required": [
                        "someArray",
                        "someList",
                        "someMap",
                        "someSet"
                      ],
                      "properties": {
                        "someArray": {
                          "type": "array",
                          "items": {
                            "type": "integer",
                            "minimum": -2147483648,
                            "maximum": 2147483647
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
                            "minimum": -2147483648,
                            "maximum": 2147483647
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
                expectedResultReferencing = """
                    {
                      "type": "object",
                      "required": [
                        "someArray",
                        "someList",
                        "someMap",
                        "someSet"
                      ],
                      "properties": {
                        "someArray": {
                          "type": "array",
                          "items": {
                            "type": "integer",
                            "minimum": -2147483648,
                            "maximum": 2147483647
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
                            "minimum": -2147483648,
                            "maximum": 2147483647
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
                expectedResultReferencingRoot = """
                    {
                      "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithCollections",
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithCollections": {
                          "type": "object",
                          "required": [
                            "someArray",
                            "someList",
                            "someMap",
                            "someSet"
                          ],
                          "properties": {
                            "someArray": {
                              "type": "array",
                              "items": {
                                "type": "integer",
                                "minimum": -2147483648,
                                "maximum": 2147483647
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
                                "minimum": -2147483648,
                                "maximum": 2147483647
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
                      }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassDirectSelfReferencing>(),
                testName = "class with direct self reference",
                expectedResultInlining = """
                    {
                      "type": "object",
                      "required": [],
                      "properties": {
                        "self": {
                          "type": "object",
                          "required": [],
                          "properties": {
                            "self": {
                              "type": "object",
                              "required": [],
                              "properties": {
                                "self": {
                                  "type": "object",
                                  "required": [],
                                  "properties": {
                                    "self": {
                                      "type": "object",
                                      "required": [],
                                      "properties": {
                                        "self": {
                                          "type": "object",
                                          "required": [],
                                          "properties": {
                                            "self": {
                                              "type": "object",
                                              "required": [],
                                              "properties": {
                                                "self": {
                                                  "type": "object",
                                                  "required": [],
                                                  "properties": {
                                                    "self": {
                                                      "type": "object",
                                                      "required": [],
                                                      "properties": {
                                                        "self": {
                                                          "type": "object",
                                                          "required": [],
                                                          "properties": {
                                                            "self": {
                                                              "type": "object",
                                                              "required": [],
                                                              "properties": {
                                                                "self": {
                                                                  "type": "object",
                                                                  "required": [],
                                                                  "properties": {
                                                                    "self": {
                                                                      "type": "object",
                                                                      "required": [],
                                                                      "properties": {
                                                                        "self": {
                                                                          "type": "object",
                                                                          "required": [],
                                                                          "properties": {
                                                                            "self": {
                                                                              "type": "object",
                                                                              "required": [],
                                                                              "properties": {
                                                                                "self": {
                                                                                  "type": "object",
                                                                                  "required": [],
                                                                                  "properties": {
                                                                                    "self": {
                                                                                      "type": "object",
                                                                                      "required": [],
                                                                                      "properties": {
                                                                                        "self": {
                                                                                          "type": "object",
                                                                                          "required": [],
                                                                                          "properties": {
                                                                                            "self": {
                                                                                              "type": "object",
                                                                                              "required": [],
                                                                                              "properties": {
                                                                                                "self": {
                                                                                                  "type": "object",
                                                                                                  "required": [],
                                                                                                  "properties": {
                                                                                                    "self": {
                                                                                                      "type": "object",
                                                                                                      "required": [],
                                                                                                      "properties": {
                                                                                                        "self": {
                                                                                                          "type": "object",
                                                                                                          "required": [],
                                                                                                          "properties": {
                                                                                                            "self": {}
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
                expectedResultReferencing = """
                    {
                      "type": "object",
                      "required": [],
                      "properties": {
                        "self": {
                          "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing"
                        }
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing": {
                          "type": "object",
                          "required": [],
                          "properties": {
                            "self": {
                              "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing"
                            }
                          }
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                      "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing",
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing": {
                          "type": "object",
                          "required": [],
                          "properties": {
                            "self": {
                              "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing"
                            }
                          }
                        }
                      }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithOptionalParameters>(),
                testName = "optional parameters as required",
                generatorConfig = {
                    optionalHandling = OptionalHandling.REQUIRED
                },
                expectedResultInlining = """
                    {
                      "type": "object",
                      "required": [
                        "ctorRequired",
                        "ctorOptional"
                      ],
                      "properties": {
                        "ctorOptional": {
                          "type": "string"
                        },
                        "ctorOptionalNullable": {
                          "type": "string"
                        },
                        "ctorRequired": {
                          "type": "string"
                        },
                        "ctorRequiredNullable": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "type": "object",
                      "required": [
                        "ctorRequired",
                        "ctorOptional"
                      ],
                      "properties": {
                        "ctorOptional": {
                          "type": "string"
                        },
                        "ctorOptionalNullable": {
                          "type": "string"
                        },
                        "ctorRequired": {
                          "type": "string"
                        },
                        "ctorRequiredNullable": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                      "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters",
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters": {
                          "type": "object",
                          "required": [
                            "ctorRequired",
                            "ctorOptional"
                          ],
                          "properties": {
                            "ctorOptional": {
                              "type": "string"
                            },
                            "ctorOptionalNullable": {
                              "type": "string"
                            },
                            "ctorRequired": {
                              "type": "string"
                            },
                            "ctorRequiredNullable": {
                              "type": "string"
                            }
                          }
                        }
                      }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithOptionalParameters>(),
                testName = "optional parameters as non-required",
                generatorConfig = {
                    optionalHandling = OptionalHandling.NON_REQUIRED
                },
                expectedResultInlining = """
                    {
                      "type": "object",
                      "required": [
                        "ctorRequired"
                      ],
                      "properties": {
                        "ctorOptional": {
                          "type": "string"
                        },
                        "ctorOptionalNullable": {
                          "type": "string"
                        },
                        "ctorRequired": {
                          "type": "string"
                        },
                        "ctorRequiredNullable": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "type": "object",
                      "required": [
                        "ctorRequired"
                      ],
                      "properties": {
                        "ctorOptional": {
                          "type": "string"
                        },
                        "ctorOptionalNullable": {
                          "type": "string"
                        },
                        "ctorRequired": {
                          "type": "string"
                        },
                        "ctorRequiredNullable": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                      "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters",
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters": {
                          "type": "object",
                          "required": [
                            "ctorRequired"
                          ],
                          "properties": {
                            "ctorOptional": {
                              "type": "string"
                            },
                            "ctorOptionalNullable": {
                              "type": "string"
                            },
                            "ctorRequired": {
                              "type": "string"
                            },
                            "ctorRequiredNullable": {
                              "type": "string"
                            }
                          }
                        }
                      }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = typeOf<ClassWithValueClass>(),
                testName = "inline value class",
                expectedResultInlining = """
                    {
                      "type": "object",
                      "required": [
                        "myValue",
                        "someText"
                      ],
                      "properties": {
                        "myValue": {
                          "type": "integer",
                          "minimum": -2147483648,
                          "maximum": 2147483647
                        },
                        "someText": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "type": "object",
                      "required": [
                        "myValue",
                        "someText"
                      ],
                      "properties": {
                        "myValue": {
                          "type": "integer",
                          "minimum": -2147483648,
                          "maximum": 2147483647
                        },
                        "someText": {
                          "type": "string"
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                      "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithValueClass",
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithValueClass": {
                          "type": "object",
                          "required": [
                            "myValue",
                            "someText"
                          ],
                          "properties": {
                            "myValue": {
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
                """.trimIndent(),
            ),
        )

    }

}