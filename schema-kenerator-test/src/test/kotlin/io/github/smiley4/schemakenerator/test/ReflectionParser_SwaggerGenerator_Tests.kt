package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.reflection.CustomReflectionTypeParser
import io.github.smiley4.schemakenerator.reflection.getKType
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerator
import io.github.smiley4.schemakenerator.swagger.module.AutoTitleModule
import io.github.smiley4.schemakenerator.swagger.module.CoreAnnotationsModule
import io.github.smiley4.schemakenerator.swagger.module.InliningGenerator
import io.github.smiley4.schemakenerator.swagger.module.ReferencingGenerator
import io.github.smiley4.schemakenerator.swagger.module.SwaggerSchemaGeneratorModule
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithNestedClass
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithDeepGeneric
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithGenericField
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithLocalDateTime
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
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KType

@Suppress("ClassName")
class ReflectionParser_SwaggerGenerator_Tests : FunSpec({

    context("generator: inlining") {
        withData(TEST_DATA) { data ->
            val context = TypeDataContext()
            val resultParser = ReflectionTypeParser(context = context, config = {
                data.customParsers.forEach { (type, parser) -> registerParser(type, parser) }
            }).parse(data.type)
            val generatorResult = SwaggerSchemaGenerator()
                .withModule(InliningGenerator())
                .withModules(data.generatorModules)
                .generate(resultParser, context)
            json.writeValueAsString(generatorResult).shouldEqualJson {
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
            val context = TypeDataContext()
            val resultParser = ReflectionTypeParser(context = context, config = {
                data.customParsers.forEach { (type, parser) -> registerParser(type, parser) }
            }).parse(data.type)
            val generatorResult = SwaggerSchemaGenerator()
                .withModule(ReferencingGenerator(referenceRoot = false))
                .withModules(data.generatorModules)
                .generate(resultParser, context)
            json.writeValueAsString(generatorResult).shouldEqualJson {
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
            val context = TypeDataContext()
            val resultParser = ReflectionTypeParser(context = context, config = {
                data.customParsers.forEach { (type, parser) -> registerParser(type, parser) }
            }).parse(data.type)
            val generatorResult = SwaggerSchemaGenerator()
                .withModule(ReferencingGenerator(referenceRoot = true))
                .withModules(data.generatorModules)
                .generate(resultParser, context)
            json.writeValueAsString(generatorResult).shouldEqualJson {
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

        val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!

        private class TestData(
            val testName: String,
            val type: KType,
            val generatorModules: List<SwaggerSchemaGeneratorModule> = emptyList(),
            val customParsers: Map<KClass<*>, CustomReflectionTypeParser> = emptyMap(),
            val expectedResultInlining: String,
            val expectedResultReferencing: String,
            val expectedResultReferencingRoot: String,
        ) : WithDataTestName {
            override fun dataTestName() = testName
        }

        private val TEST_DATA = listOf(
            TestData(
                type = getKType<Any>(),
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
                type = getKType<UByte>(),
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
                type = getKType<Int>(),
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
                type = getKType<Float>(),
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
                type = getKType<Boolean>(),
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
                type = getKType<String>(),
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
                type = getKType<List<String>>(),
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
                type = getKType<Map<String, Int>>(),
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
                type = getKType<ClassWithSimpleFields>(),
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
                            "${'$'}ref": "#/definitions/ClassWithSimpleFields",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "ClassWithSimpleFields": {
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
                type = getKType<TestEnum>(),
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
                            "${'$'}ref": "#/definitions/TestEnum",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "TestEnum": {
                                "exampleSetFlag": false,
                                "enum": [ "ONE", "TWO", "THREE" ]
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = getKType<ClassWithGenericField<String>>(),
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
                            "${'$'}ref": "#/definitions/ClassWithGenericField<String>",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "ClassWithGenericField<String>": {
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
                type = getKType<ClassWithGenericField<*>>(),
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
                            "${'$'}ref": "#/definitions/ClassWithGenericField<*>",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "ClassWithGenericField<*>": {
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
                type = getKType<ClassWithDeepGeneric<String>>(),
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
                            "${'$'}ref": "#/definitions/ClassWithDeepGeneric<String>",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "ClassWithDeepGeneric<String>": {
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
                type = getKType<SealedClass>(),
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
                                    "${'$'}ref": "#/definitions/SubClassA",
                                    "exampleSetFlag": false
                                },
                                {
                                    "${'$'}ref": "#/definitions/SubClassB",
                                    "exampleSetFlag": false
                                }
                            ]
                        },
                        "definitions": {
                            "SubClassA": {
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
                            "SubClassB": {
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
                            "${'$'}ref": "#/definitions/SealedClass",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "SealedClass": {
                                "exampleSetFlag": false,
                                "anyOf": [
                                    {
                                        "${'$'}ref": "#/definitions/SubClassA",
                                        "exampleSetFlag": false
                                    },
                                    {
                                        "${'$'}ref": "#/definitions/SubClassB",
                                        "exampleSetFlag": false
                                    }
                                ]
                            },
                            "SubClassA": {
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
                            "SubClassB": {
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
                type = getKType<SubClassA>(),
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
                            "${'$'}ref": "#/definitions/SubClassA",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "SubClassA": {
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
                type = getKType<CoreAnnotatedClass>(),
                testName = "annotated class (core)",
                generatorModules = listOf(CoreAnnotationsModule()),
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
                            "${'$'}ref": "#/definitions/CoreAnnotatedClass",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "CoreAnnotatedClass": {
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
            TestData(
                type = getKType<ClassWithLocalDateTime>(),
                testName = "class with java local-date-time and custom parser",
                customParsers = mapOf(
                    LocalDateTime::class to CustomReflectionTypeParser { typeId, _ ->
                        PrimitiveTypeData(
                            id = typeId,
                            simpleName = String::class.simpleName!!,
                            qualifiedName = String::class.qualifiedName!!,
                            typeParameters = mutableMapOf()
                        )
                    }
                ),
                expectedResultInlining = """
                    {
                        "schema": {
                            "required": [
                                "dateTime"
                            ],
                            "type": "object",
                            "properties": {
                                "dateTime": {
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
                                "dateTime"
                            ],
                            "type": "object",
                            "properties": {
                                "dateTime": {
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
                            "${'$'}ref": "#/definitions/ClassWithLocalDateTime",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "ClassWithLocalDateTime": {
                                "required": [
                                    "dateTime"
                                ],
                                "type": "object",
                                "properties": {
                                    "dateTime": {
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
                type = getKType<ClassWithNestedClass>(),
                testName = "auto title",
                generatorModules = listOf(
                    AutoTitleModule(type = AutoTitleModule.Companion.AutoTitleType.SIMPLE_NAME)
                ),
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
                                    "${'$'}ref": "#/definitions/NestedClass",
                                    "exampleSetFlag": false
                                }
                            },
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "NestedClass": {
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
                            "${'$'}ref": "#/definitions/ClassWithNestedClass",
                            "exampleSetFlag": false
                        },
                        "definitions": {
                            "NestedClass": {
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
                            "ClassWithNestedClass": {
                                "title": "ClassWithNestedClass",
                                "required": [
                                    "nested"
                                ],
                                "type": "object",
                                "properties": {
                                    "nested": {
                                        "${'$'}ref": "#/definitions/NestedClass",
                                        "exampleSetFlag": false
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