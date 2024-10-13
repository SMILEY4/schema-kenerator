package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.reflection.steps.ReflectionTypeProcessingStep
import io.github.smiley4.schemakenerator.swagger.OptionalHandling
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerationStepConfig
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerArraySchemaAnnotationStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaAnnotationStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceRootStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCompileReferenceStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDefaultStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDeprecatedStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationDescriptionStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationExamplesStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaCoreAnnotationTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaGenerationStep
import io.github.smiley4.schemakenerator.swagger.steps.SwaggerSchemaTitleStep
import io.github.smiley4.schemakenerator.swagger.steps.TitleBuilder
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
                .let {
                    SwaggerSchemaGenerationStep(
                        optionalAsNonRequired = SwaggerSchemaGenerationStepConfig().apply(data.generatorConfig).optionalHandling == OptionalHandling.NON_REQUIRED
                    ).generate(it)
                }
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .let { SwaggerSchemaCoreAnnotationTitleStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDescriptionStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDefaultStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationExamplesStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDeprecatedStep().process(it) }
                            .let { SwaggerSchemaAnnotationStep().process(it) }
                            .let { SwaggerArraySchemaAnnotationStep().process(it) }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { SwaggerSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
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
                .let {
                    SwaggerSchemaGenerationStep(
                        optionalAsNonRequired = SwaggerSchemaGenerationStepConfig().apply(data.generatorConfig).optionalHandling == OptionalHandling.NON_REQUIRED
                    ).generate(it)
                }
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .let { SwaggerSchemaCoreAnnotationTitleStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDescriptionStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDefaultStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationExamplesStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDeprecatedStep().process(it) }
                            .let { SwaggerSchemaAnnotationStep().process(it) }
                            .let { SwaggerArraySchemaAnnotationStep().process(it) }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { SwaggerSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { SwaggerSchemaCompileReferenceStep(TitleBuilder.BUILDER_FULL).compile(it) }
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
                .let {
                    SwaggerSchemaGenerationStep(
                        optionalAsNonRequired = SwaggerSchemaGenerationStepConfig().apply(data.generatorConfig).optionalHandling == OptionalHandling.NON_REQUIRED
                    ).generate(it)
                }
                .let { list ->
                    if (data.withAnnotations) {
                        list
                            .let { SwaggerSchemaCoreAnnotationTitleStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDescriptionStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDefaultStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationExamplesStep().process(it) }
                            .let { SwaggerSchemaCoreAnnotationDeprecatedStep().process(it) }
                            .let { SwaggerSchemaAnnotationStep().process(it) }
                            .let { SwaggerArraySchemaAnnotationStep().process(it) }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { SwaggerSchemaTitleStep(TitleBuilder.BUILDER_SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { SwaggerSchemaCompileReferenceRootStep(TitleBuilder.BUILDER_FULL).compile(it) }
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
            val generatorConfig: SwaggerSchemaGenerationStepConfig.() -> Unit = {},
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
                            "types": ["object"],
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["object"],
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["object"],
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
                            "types": ["integer"],
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
                            "types": ["integer"],
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
                            "types": ["integer"],
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
                            "types": ["integer"],
                            "format": "int32",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["integer"],
                            "format": "int32",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["integer"],
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
                            "types": ["number"],
                            "format": "float",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["number"],
                            "format": "float",
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["number"],
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
                            "types": ["boolean"],
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["boolean"],
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["boolean"],
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
                            "types": ["string"],
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["string"],
                            "exampleSetFlag": false
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["string"],
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
                            "types": ["array"],
                            "exampleSetFlag": false,
                            "items": {
                                "types": ["string"],
                                "exampleSetFlag": false
                            }
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "schema": {
                            "types": ["array"],
                            "exampleSetFlag": false,
                            "items": {
                                "types": ["string"],
                                "exampleSetFlag": false
                            }
                        },
                        "definitions": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "schema": {
                            "types": ["array"],
                            "exampleSetFlag": false,
                            "items": {
                                "types": ["string"],
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
                            "types": ["object"],
                            "additionalProperties": {
                                "types": ["integer"],
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
                            "types": ["object"],
                            "additionalProperties": {
                                "types": ["integer"],
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
                            "types": ["object"],
                            "additionalProperties": {
                                "types": ["integer"],
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
                            "types": ["object"],
                            "properties": {
                                "someBoolList": {
                                    "types": ["array"],
                                    "exampleSetFlag": false,
                                    "items": {
                                      "types": ["boolean"],
                                      "exampleSetFlag": false
                                    }
                                },
                                "someNullableInt": {
                                    "types": ["integer", "null"],
                                    "format": "int32",
                                    "exampleSetFlag": false
                                },
                                "someString": {
                                    "types": ["string"],
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
                            "types": ["object"],
                            "properties": {
                                "someBoolList": {
                                    "types": ["array"],
                                    "exampleSetFlag": false,
                                    "items": {
                                      "types": ["boolean"],
                                      "exampleSetFlag": false
                                    }
                                },
                                "someNullableInt": {
                                    "types": ["integer", "null"],
                                    "format": "int32",
                                    "exampleSetFlag": false
                                },
                                "someString": {
                                    "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "someBoolList": {
                                        "types": ["array"],
                                        "exampleSetFlag": false,
                                        "items": {
                                            "types": ["boolean"],
                                            "exampleSetFlag": false
                                        }
                                    },
                                    "someNullableInt": {
                                        "types": ["integer", "null"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "someString": {
                                        "types": ["string"],
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
                            "types": ["object"],
                            "properties": {
                                "value": {
                                    "types": ["string"],
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
                            "types": ["object"],
                            "properties": {
                                "value": {
                                    "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "value": {
                                        "types": ["string"],
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
                            "types": ["object"],
                            "properties": {
                                "value": {
                                    "types": ["object"],
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
                            "types": ["object"],
                            "properties": {
                                "value": {
                                    "types": ["object"],
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
                                "types": ["object"],
                                "properties": {
                                    "value": {
                                        "types": ["object"],
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
                            "types": ["object"],
                            "properties": {
                                "value": {
                                    "types": ["array"],
                                    "exampleSetFlag": false,
                                    "items": {
                                        "types": ["string"],
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
                            "types": ["object"],
                            "properties": {
                                "value": {
                                    "types": ["array"],
                                    "exampleSetFlag": false,
                                    "items": {
                                        "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "value": {
                                        "types": ["array"],
                                        "exampleSetFlag": false,
                                        "items": {
                                            "types": ["string"],
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
                                    "types": ["object"],
                                    "properties": {
                                        "a": {
                                              "types": ["integer"],
                                              "format": "int32",
                                              "exampleSetFlag": false
                                            },
                                        "sealedValue": {
                                            "types": ["string"],
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
                                    "types": ["object"],
                                    "properties": {
                                        "b": {
                                            "types": ["integer"],
                                            "format": "int32",
                                            "exampleSetFlag": false
                                        },
                                        "sealedValue": {
                                            "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "a": {
                                        "types": ["integer"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "b": {
                                        "types": ["integer"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "a": {
                                        "types": ["integer"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "b": {
                                        "types": ["integer"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "types": ["string"],
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
                            "types": ["object"],
                            "properties": {
                                "a": {
                                    "types": ["integer"],
                                    "format": "int32",
                                    "exampleSetFlag": false
                                },
                                "sealedValue": {
                                    "types": ["string"],
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
                            "types": ["object"],
                            "properties": {
                                "a": {
                                    "types": ["integer"],
                                    "format": "int32",
                                    "exampleSetFlag": false
                                },
                                "sealedValue": {
                                    "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "a": {
                                        "types": ["integer"],
                                        "format": "int32",
                                        "exampleSetFlag": false
                                    },
                                    "sealedValue": {
                                        "types": ["string"],
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
                                "stringValue",
                                "intValue"
                            ],
                            "types": ["object"],
                            "properties": {
                                "stringValue": {
                                    "types": ["string"],
                                    "description": "String field description",
                                    "default": "A default String value",
                                    "exampleSetFlag": true,
                                    "example": "An example of a String value"
                                },
                                "intValue": {
                                    "types": ["integer"],
                                    "format": "int32",
                                    "description": "Int field description",
                                    "default": "1111",
                                    "exampleSetFlag": true,
                                    "example": "2222"
                                }
                            },
                            "description": "some description",
                            "deprecated": true,
                            "exampleSetFlag": true,
                            "example": "example 1",
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
                                "stringValue",
                                "intValue"
                            ],
                            "types": ["object"],
                            "properties": {
                                "stringValue": {
                                    "types": ["string"],
                                    "description": "String field description",
                                    "default": "A default String value",
                                    "exampleSetFlag": true,
                                    "example": "An example of a String value"
                                },
                                "intValue": {
                                    "types": ["integer"],
                                    "format": "int32",
                                    "description": "Int field description",
                                    "default": "1111",
                                    "exampleSetFlag": true,
                                    "example": "2222"
                                }
                            },
                            "description": "some description",
                            "deprecated": true,
                            "exampleSetFlag": true,
                            "example": "example 1",
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
                                    "stringValue",
                                    "intValue"
                                ],
                                "types": ["object"],
                                "properties": {
                                    "stringValue": {
                                        "types": ["string"],
                                        "description": "String field description",
                                        "default": "A default String value",
                                        "exampleSetFlag": true,
                                        "example": "An example of a String value"
                                    },
                                    "intValue": {
                                        "types": ["integer"],
                                        "format": "int32",
                                        "description": "Int field description",
                                        "default": "1111",
                                        "exampleSetFlag": true,
                                        "example": "2222"
                                    }
                                },
                                "description": "some description",
                                "deprecated": true,
                                "exampleSetFlag": true,
                                "example": "example 1",
                                "default": "default value"
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
                        "schema": {
                            "title": "ClassWithNestedClass",
                            "required": [
                                "nested"
                            ],
                            "types": ["object"],
                            "properties": {
                                "nested": {
                                    "title": "NestedClass",
                                    "required": [
                                        "text"
                                    ],
                                    "types": ["object"],
                                    "properties": {
                                        "text": {
                                            "title": "String",
                                            "types": ["string"],
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
                            "types": ["object"],
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
                                "types": ["object"],
                                "properties": {
                                    "text": {
                                        "title": "String",
                                        "types": ["string"],
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
                                "types": ["object"],
                                "properties": {
                                    "text": {
                                        "title": "String",
                                        "types": ["string"],
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
                                "types": ["object"],
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
                        "types": ["object"],
                        "properties": {
                          "someArray": {
                            "types": ["array"],
                            "exampleSetFlag": false,
                            "items": {
                              "types": ["integer"],
                              "format": "int32",
                              "exampleSetFlag": false
                            }
                          },
                          "someList": {
                            "types": ["array"],
                            "exampleSetFlag": false,
                            "items": {
                              "types": ["string"],
                              "exampleSetFlag": false
                            }
                          },
                          "someMap": {
                            "types": ["object"],
                            "additionalProperties": {
                              "types": ["integer"],
                              "format": "int32",
                              "exampleSetFlag": false
                            },
                            "exampleSetFlag": false
                          },
                          "someSet": {
                            "types": ["array"],
                            "uniqueItems": true,
                            "exampleSetFlag": false,
                            "items": {
                              "types": ["string"],
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
                        "types": ["object"],
                        "properties": {
                          "someArray": {
                            "types": ["array"],
                            "exampleSetFlag": false,
                            "items": {
                              "types": ["integer"],
                              "format": "int32",
                              "exampleSetFlag": false
                            }
                          },
                          "someList": {
                            "types": ["array"],
                            "exampleSetFlag": false,
                            "items": {
                              "types": ["string"],
                              "exampleSetFlag": false
                            }
                          },
                          "someMap": {
                            "types": ["object"],
                            "additionalProperties": {
                              "types": ["integer"],
                              "format": "int32",
                              "exampleSetFlag": false
                            },
                            "exampleSetFlag": false
                          },
                          "someSet": {
                            "types": ["array"],
                            "uniqueItems": true,
                            "exampleSetFlag": false,
                            "items": {
                              "types": ["string"],
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
                          "types": ["object"],
                          "properties": {
                            "someArray": {
                              "types": ["array"],
                              "exampleSetFlag": false,
                              "items": {
                                "types": ["integer"],
                                "format": "int32",
                                "exampleSetFlag": false
                              }
                            },
                            "someList": {
                              "types": ["array"],
                              "exampleSetFlag": false,
                              "items": {
                                "types": ["string"],
                                "exampleSetFlag": false
                              }
                            },
                            "someMap": {
                              "types": ["object"],
                              "additionalProperties": {
                                "types": ["integer"],
                                "format": "int32",
                                "exampleSetFlag": false
                              },
                              "exampleSetFlag": false
                            },
                            "someSet": {
                              "types": ["array"],
                              "uniqueItems": true,
                              "exampleSetFlag": false,
                              "items": {
                                "types": ["string"],
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
                type = typeOf<ClassDirectSelfReferencing>(),
                testName = "class with direct self reference",
                expectedResultInlining = """
                    {
                      "schema": {
                        "types": ["object"],
                        "properties": {
                          "self": {
                            "types": ["object", "null"],
                            "properties": {
                              "self": {
                                "types": ["object", "null"],
                                "properties": {
                                  "self": {
                                    "types": ["object", "null"],
                                    "properties": {
                                      "self": {
                                        "types": ["object", "null"],
                                        "properties": {
                                          "self": {
                                            "types": ["object", "null"],
                                            "properties": {
                                              "self": {
                                                "types": ["object", "null"],
                                                "properties": {
                                                  "self": {
                                                    "types": ["object", "null"],
                                                    "properties": {
                                                      "self": {
                                                        "types": ["object", "null"],
                                                        "properties": {
                                                          "self": {
                                                            "types": ["object", "null"],
                                                            "properties": {
                                                              "self": {
                                                                "types": ["object", "null"],
                                                                "properties": {
                                                                  "self": {
                                                                    "types": ["object", "null"],
                                                                    "properties": {
                                                                      "self": {
                                                                        "types": ["object", "null"],
                                                                        "properties": {
                                                                          "self": {
                                                                            "types": ["object", "null"],
                                                                            "properties": {
                                                                              "self": {
                                                                                "types": ["object", "null"],
                                                                                "properties": {
                                                                                  "self": {
                                                                                    "types": ["object", "null"],
                                                                                    "properties": {
                                                                                      "self": {
                                                                                        "types": ["object", "null"],
                                                                                        "properties": {
                                                                                          "self": {
                                                                                            "types": ["object", "null"],
                                                                                            "properties": {
                                                                                              "self": {
                                                                                                "types": ["object", "null"],
                                                                                                "properties": {
                                                                                                  "self": {
                                                                                                    "types": ["object", "null"],
                                                                                                    "properties": {
                                                                                                      "self": {
                                                                                                        "types": ["object", "null"],
                                                                                                        "properties": {
                                                                                                          "self": {
                                                                                                            "types": ["object", "null"],
                                                                                                            "properties": {
                                                                                                              "self": {
                                                                                                                "types": ["object", "null"],
                                                                                                                "properties": {
                                                                                                                  "self": {
                                                                                                                    "types": ["object", "null"],
                                                                                                                    "properties": {
                                                                                                                      "self": {
                                                                                                                        "types": ["object", "null"],
                                                                                                                        "properties": {
                                                                                                                          "self": {
                                                                                                                            "types": ["object", "null"],
                                                                                                                            "properties": {
                                                                                                                              "self": {
                                                                                                                                "types": ["object", "null"],
                                                                                                                                "properties": {
                                                                                                                                  "self": {
                                                                                                                                    "types": ["object", "null"],
                                                                                                                                    "properties": {
                                                                                                                                      "self": {
                                                                                                                                        "types": ["object", "null"],
                                                                                                                                        "properties": {
                                                                                                                                          "self": {
                                                                                                                                            "types": ["object", "null"],
                                                                                                                                            "properties": {
                                                                                                                                              "self": {
                                                                                                                                                "types": ["object", "null"],
                                                                                                                                                "properties": {
                                                                                                                                                  "self": {
                                                                                                                                                    "types": ["object", "null"],
                                                                                                                                                    "properties": {
                                                                                                                                                      "self": {
                                                                                                                                                        "types": ["object", "null"],
                                                                                                                                                        "properties": {
                                                                                                                                                          "self": {
                                                                                                                                                            "exampleSetFlag": false
                                                                                                                                                          }
                                                                                                                                                        },
                                                                                                                                                        "exampleSetFlag": false
                                                                                                                                                      }
                                                                                                                                                    },
                                                                                                                                                    "exampleSetFlag": false
                                                                                                                                                  }
                                                                                                                                                },
                                                                                                                                                "exampleSetFlag": false
                                                                                                                                              }
                                                                                                                                            },
                                                                                                                                            "exampleSetFlag": false
                                                                                                                                          }
                                                                                                                                        },
                                                                                                                                        "exampleSetFlag": false
                                                                                                                                      }
                                                                                                                                    },
                                                                                                                                    "exampleSetFlag": false
                                                                                                                                  }
                                                                                                                                },
                                                                                                                                "exampleSetFlag": false
                                                                                                                              }
                                                                                                                            },
                                                                                                                            "exampleSetFlag": false
                                                                                                                          }
                                                                                                                        },
                                                                                                                        "exampleSetFlag": false
                                                                                                                      }
                                                                                                                    },
                                                                                                                    "exampleSetFlag": false
                                                                                                                  }
                                                                                                                },
                                                                                                                "exampleSetFlag": false
                                                                                                              }
                                                                                                            },
                                                                                                            "exampleSetFlag": false
                                                                                                          }
                                                                                                        },
                                                                                                        "exampleSetFlag": false
                                                                                                      }
                                                                                                    },
                                                                                                    "exampleSetFlag": false
                                                                                                  }
                                                                                                },
                                                                                                "exampleSetFlag": false
                                                                                              }
                                                                                            },
                                                                                            "exampleSetFlag": false
                                                                                          }
                                                                                        },
                                                                                        "exampleSetFlag": false
                                                                                      }
                                                                                    },
                                                                                    "exampleSetFlag": false
                                                                                  }
                                                                                },
                                                                                "exampleSetFlag": false
                                                                              }
                                                                            },
                                                                            "exampleSetFlag": false
                                                                          }
                                                                        },
                                                                        "exampleSetFlag": false
                                                                      }
                                                                    },
                                                                    "exampleSetFlag": false
                                                                  }
                                                                },
                                                                "exampleSetFlag": false
                                                              }
                                                            },
                                                            "exampleSetFlag": false
                                                          }
                                                        },
                                                        "exampleSetFlag": false
                                                      }
                                                    },
                                                    "exampleSetFlag": false
                                                  }
                                                },
                                                "exampleSetFlag": false
                                              }
                                            },
                                            "exampleSetFlag": false
                                          }
                                        },
                                        "exampleSetFlag": false
                                      }
                                    },
                                    "exampleSetFlag": false
                                  }
                                },
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
                        "types": ["object"],
                        "properties": {
                          "self": {
                            "oneOf": [
                              {
                                "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing",
                                "exampleSetFlag": false
                              },
                              {
                                "types": ["null"],
                                "exampleSetFlag": false
                              }
                            ],
                            "exampleSetFlag": false
                          }
                        },
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing": {
                          "types": ["object"],
                          "properties": {
                            "self": {
                              "oneOf": [
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing",
                                  "exampleSetFlag": false
                                },
                                {
                                  "types": ["null"],
                                  "exampleSetFlag": false
                                }
                              ],
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
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing",
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing": {
                          "types": ["object"],
                          "properties": {
                            "self": {
                              "oneOf": [
                                {
                                  "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassDirectSelfReferencing",
                                  "exampleSetFlag": false
                                },
                                {
                                  "types": ["null"],
                                  "exampleSetFlag": false
                                }
                              ],
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
                type = typeOf<ClassWithOptionalParameters>(),
                testName = "optional parameters as required",
                generatorConfig = {
                    optionalHandling = OptionalHandling.REQUIRED
                },
                expectedResultInlining = """
                    {
                      "schema": {
                        "required": [
                          "ctorOptional",
                          "ctorRequired"
                        ],
                        "types": ["object"],
                        "properties": {
                          "ctorOptional": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          },
                          "ctorOptionalNullable": {
                            "types": ["string", "null"],
                            "exampleSetFlag": false
                          },
                          "ctorRequired": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          },
                          "ctorRequiredNullable": {
                            "types": ["string", "null"],
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
                          "ctorOptional",
                          "ctorRequired"
                        ],
                        "types": ["object"],
                        "properties": {
                          "ctorOptional": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          },
                          "ctorOptionalNullable": {
                            "types": ["string", "null"],
                            "exampleSetFlag": false
                          },
                          "ctorRequired": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          },
                          "ctorRequiredNullable": {
                            "types": ["string", "null"],
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
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters",
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters": {
                          "required": [
                            "ctorOptional",
                            "ctorRequired"
                          ],
                          "types": ["object"],
                          "properties": {
                            "ctorOptional": {
                              "types": ["string"],
                              "exampleSetFlag": false
                            },
                            "ctorOptionalNullable": {
                              "types": ["string", "null"],
                              "exampleSetFlag": false
                            },
                            "ctorRequired": {
                              "types": ["string"],
                              "exampleSetFlag": false
                            },
                            "ctorRequiredNullable": {
                              "types": ["string", "null"],
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
                type = typeOf<ClassWithOptionalParameters>(),
                testName = "optional parameters as non-required",
                generatorConfig = {
                    optionalHandling = OptionalHandling.NON_REQUIRED
                },
                expectedResultInlining = """
                    {
                      "schema": {
                        "required": [
                          "ctorRequired"
                        ],
                        "types": ["object"],
                        "properties": {
                          "ctorOptional": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          },
                          "ctorOptionalNullable": {
                            "types": ["string", "null"],
                            "exampleSetFlag": false
                          },
                          "ctorRequired": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          },
                          "ctorRequiredNullable": {
                            "types": ["string", "null"],
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
                          "ctorRequired"
                        ],
                        "types": ["object"],
                        "properties": {
                          "ctorOptional": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          },
                          "ctorOptionalNullable": {
                            "types": ["string", "null"],
                            "exampleSetFlag": false
                          },
                          "ctorRequired": {
                            "types": ["string"],
                            "exampleSetFlag": false
                          },
                          "ctorRequiredNullable": {
                            "types": ["string", "null"],
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
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters",
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithOptionalParameters": {
                          "required": [
                            "ctorRequired"
                          ],
                          "types": ["object"],
                          "properties": {
                            "ctorOptional": {
                              "types": ["string"],
                              "exampleSetFlag": false
                            },
                            "ctorOptionalNullable": {
                              "types": ["string", "null"],
                              "exampleSetFlag": false
                            },
                            "ctorRequired": {
                              "types": ["string"],
                              "exampleSetFlag": false
                            },
                            "ctorRequiredNullable": {
                              "types": ["string", "null"],
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
                type = typeOf<ClassWithValueClass>(),
                testName = "inline value class",
                expectedResultInlining = """
                    {
                      "schema": {
                        "required": [
                          "myValue",
                          "someText"
                        ],
                        "types": ["object"],
                        "properties": {
                          "myValue": {
                            "types": ["integer"],
                            "format": "int32",
                            "exampleSetFlag": false
                          },
                          "someText": {
                            "types": ["string"],
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
                          "myValue",
                          "someText"
                        ],
                        "types": ["object"],
                        "properties": {
                          "myValue": {
                            "types": ["integer"],
                            "format": "int32",
                            "exampleSetFlag": false
                          },
                          "someText": {
                            "types": ["string"],
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
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithValueClass",
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithValueClass": {
                          "required": [
                            "myValue",
                            "someText"
                          ],
                          "types": ["object"],
                          "properties": {
                            "myValue": {
                              "types": ["integer"],
                              "format": "int32",
                              "exampleSetFlag": false
                            },
                            "someText": {
                              "types": ["string"],
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
                type = typeOf<ClassWithAnnotatedValueClass>(),
                testName = "inline annotated value class",
                withAnnotations = true,
                expectedResultInlining = """
                    {
                      "schema": {
                        "required": [
                          "myValue"
                        ],
                        "type": "object",
                        "properties": {
                          "myValue": {
                            "maximum": 15,
                            "exclusiveMaximum": false,
                            "minimum": 5,
                            "exclusiveMinimum": false,
                            "maxLength": 2147483647,
                            "minLength": 0,
                            "type": "string",
                            "description": "annotated value class for testing.",
                            "format": "",
                            "exampleSetFlag": false,
                            "default": "default on property"
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
                          "myValue"
                        ],
                        "type": "object",
                        "properties": {
                          "myValue": {
                            "maximum": 15,
                            "exclusiveMaximum": false,
                            "minimum": 5,
                            "exclusiveMinimum": false,
                            "maxLength": 2147483647,
                            "minLength": 0,
                            "type": "string",
                            "description": "annotated value class for testing.",
                            "format": "",
                            "exampleSetFlag": false,
                            "default": "default on property"
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
                        "${'$'}ref": "#/components/schemas/io.github.smiley4.schemakenerator.test.models.reflection.ClassWithAnnotatedValueClass",
                        "exampleSetFlag": false
                      },
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.reflection.ClassWithAnnotatedValueClass": {
                          "required": [
                            "myValue"
                          ],
                          "type": "object",
                          "properties": {
                            "myValue": {
                              "maximum": 15,
                              "exclusiveMaximum": false,
                              "minimum": 5,
                              "exclusiveMinimum": false,
                              "maxLength": 2147483647,
                              "minLength": 0,
                              "type": "string",
                              "description": "annotated value class for testing.",
                              "format": "",
                              "exampleSetFlag": false,
                              "default": "default on property"
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