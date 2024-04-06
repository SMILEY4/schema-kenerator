package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerator
import io.github.smiley4.schemakenerator.jsonschema.asJson
import io.github.smiley4.schemakenerator.jsonschema.module.AutoTitleModule
import io.github.smiley4.schemakenerator.jsonschema.module.AutoTitleModule.Companion.AutoTitleType
import io.github.smiley4.schemakenerator.jsonschema.module.CoreAnnotationsModule
import io.github.smiley4.schemakenerator.jsonschema.module.InliningGenerator
import io.github.smiley4.schemakenerator.jsonschema.module.JsonSchemaGeneratorModule
import io.github.smiley4.schemakenerator.jsonschema.module.ReferencingGenerator
import io.github.smiley4.schemakenerator.reflection.CustomReflectionTypeParser
import io.github.smiley4.schemakenerator.reflection.getKType
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
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
class ReflectionParser_JsonGenerator_Tests : FunSpec({

//    context("(manual testing)") {
//        withData(listOf(getKType<ClassWithLocalDateTime>())) { data ->
//            val context = TypeDataContext()
//            val resultParser =
//                ReflectionTypeParser(context = context, config = {
//                    inline = false
//                    registerParser(LocalDateTime::class, { typeId, _ ->
//                        PrimitiveTypeData(
//                            id = typeId,
//                            simpleName = Long::class.simpleName!!,
//                            qualifiedName = Long::class.qualifiedName!!,
//                            typeParameters = mutableMapOf()
//                        )
//                    })
//                }).parse(data)
//            val generatorResult = JsonSchemaGenerator()
//                .withModule(InliningGenerator())
//                .generate(resultParser, context)
//            println(generatorResult.asJson())
//        }
//    }

    context("basics; parser: no-inline; generator: inlining") {
        withData(TEST_DATA) { data ->
            val context = TypeDataContext()
            val resultParser =
                ReflectionTypeParser(context = context, config = {
                    inline = false
                    data.customParsers.forEach { (type, parser) -> registerParser(type, parser) }
                }).parse(data.type)
            val generatorResult = JsonSchemaGenerator()
                .withModule(InliningGenerator())
                .withModules(data.generatorModules)
                .generate(resultParser, context)
            generatorResult.asJson().prettyPrint().shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedResultInlining
            }
        }
    }

    context("basics; parser: with-inline; generator: inlining") {
        withData(TEST_DATA) { data ->
            val context = TypeDataContext()
            val resultParser = ReflectionTypeParser(context = context, config = {
                inline = true
                data.customParsers.forEach { (type, parser) -> registerParser(type, parser) }
            }).parse(data.type)
            val generatorResult = JsonSchemaGenerator()
                .withModule(InliningGenerator())
                .withModules(data.generatorModules)
                .generate(resultParser, context)
            generatorResult.asJson().prettyPrint().shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedResultInlining
            }
        }
    }

    context("basics; parser: no-inline; generator: referencing") {
        withData(TEST_DATA) { data ->
            val context = TypeDataContext()
            val resultParser = ReflectionTypeParser(context = context, config = {
                inline = false
                data.customParsers.forEach { (type, parser) -> registerParser(type, parser) }
            }).parse(data.type)
            val generatorResult = JsonSchemaGenerator()
                .withModule(ReferencingGenerator(referenceRoot = false))
                .withModules(data.generatorModules)
                .generate(resultParser, context)
            generatorResult.asJson().prettyPrint().shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedResultReferencing
            }
        }
    }

    context("basics; parser: with-inline; generator: referencing") {
        withData(TEST_DATA) { data ->
            val context = TypeDataContext()
            val resultParser = ReflectionTypeParser(context = context, config = {
                inline = true
                data.customParsers.forEach { (type, parser) -> registerParser(type, parser) }
            }).parse(data.type)
            val generatorResult = JsonSchemaGenerator()
                .withModule(ReferencingGenerator(referenceRoot = false))
                .withModules(data.generatorModules)
                .generate(resultParser, context)
            generatorResult.asJson().prettyPrint().shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedResultReferencing
            }
        }
    }

    context("basics; parser: no-inline; generator: referencing-root") {
        withData(TEST_DATA) { data ->
            val context = TypeDataContext()
            val resultParser = ReflectionTypeParser(context = context, config = {
                inline = false
                data.customParsers.forEach { (type, parser) -> registerParser(type, parser) }
            }).parse(data.type)
            val generatorResult = JsonSchemaGenerator()
                .withModule(ReferencingGenerator(referenceRoot = true))
                .withModules(data.generatorModules)
                .generate(resultParser, context)
            generatorResult.asJson().prettyPrint().shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedResultReferencingRoot
            }
        }
    }

    context("basics; parser: with-inline; generator: referencing-root") {
        withData(TEST_DATA) { data ->
            val context = TypeDataContext()
            val resultParser = ReflectionTypeParser(context = context, config = {
                inline = true
                data.customParsers.forEach { (type, parser) -> registerParser(type, parser) }
            }).parse(data.type)
            val generatorResult = JsonSchemaGenerator()
                .withModule(ReferencingGenerator(referenceRoot = true))
                .withModules(data.generatorModules)
                .generate(resultParser, context)
            generatorResult.asJson().prettyPrint().shouldEqualJson {
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

        private class TestData(
            val testName: String,
            val type: KType,
            val generatorModules: List<JsonSchemaGeneratorModule> = emptyList(),
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
                type = getKType<UByte>(),
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
                type = getKType<Int>(),
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
                type = getKType<Float>(),
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
                type = getKType<Boolean>(),
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
                type = getKType<String>(),
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
                type = getKType<List<String>>(),
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
                type = getKType<Map<String, Int>>(),
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
                type = getKType<ClassWithSimpleFields>(),
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
                        "${'$'}ref": "#/definitions/ClassWithSimpleFields",
                        "definitions": {
                            "ClassWithSimpleFields": {
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
                type = getKType<TestEnum>(),
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
                    "${'$'}ref": "#/definitions/TestEnum",
                    "definitions": {
                        "TestEnum": {
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
                    "${'$'}ref": "#/definitions/ClassWithGenericField<String>",
                    "definitions": {
                        "ClassWithGenericField<String>": {
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
                type = getKType<ClassWithGenericField<*>>(),
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
                        "${'$'}ref": "#/definitions/ClassWithGenericField<*>",
                        "definitions": {
                            "ClassWithGenericField<*>": {
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
                type = getKType<ClassWithDeepGeneric<String>>(),
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
                        "${'$'}ref": "#/definitions/ClassWithDeepGeneric<String>",
                        "definitions": {
                            "ClassWithDeepGeneric<String>": {
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
                type = getKType<SealedClass>(),
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
                                "${'$'}ref": "#/definitions/SubClassA"
                            },
                            {
                                "${'$'}ref": "#/definitions/SubClassB"
                            }
                        ],
                        "definitions": {
                            "SubClassA": {
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
                            "SubClassB": {
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
                        "${'$'}ref": "#/definitions/SealedClass",
                        "definitions": {
                            "SealedClass": {
                                "anyOf": [
                                    {
                                        "${'$'}ref": "#/definitions/SubClassA"
                                    },
                                    {
                                        "${'$'}ref": "#/definitions/SubClassB"
                                    }
                                ]
                            },
                            "SubClassA": {
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
                            "SubClassB": {
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
                type = getKType<SubClassA>(),
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
                        "${'$'}ref": "#/definitions/SubClassA",
                        "definitions": {
                            "SubClassA": {
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
                type = getKType<CoreAnnotatedClass>(),
                testName = "annotated class (core)",
                generatorModules = listOf(CoreAnnotationsModule()),
                expectedResultInlining = """
                    {
                        "type": "object",
                        "required": ["value"],
                        "properties": {
                            "value": {
                                "type": "string",
                                "description": "field description"
                            }
                        },
                        "title": "Annotated Class",
                        "description": "some description",
                        "default": "default value",
                        "examples": [
                            "example 1",
                            "example 2"
                        ],
                        "deprecated": true
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": ["value"],
                        "properties": {
                            "value": {
                                "type": "string",
                                "description": "field description"
                            }
                        },
                        "title": "Annotated Class",
                        "description": "some description",
                        "default": "default value",
                        "examples": [
                            "example 1",
                            "example 2"
                        ],
                        "deprecated": true
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/CoreAnnotatedClass",
                        "definitions": {
                            "CoreAnnotatedClass": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "string",
                                        "description": "field description"
                                    }
                                },
                                "title": "Annotated Class",
                                "description": "some description",
                                "default": "default value",
                                "examples": [
                                    "example 1",
                                    "example 2"
                                ],
                                "deprecated": true
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
                        "type": "object",
                        "required": [
                            "dateTime"
                        ],
                        "properties": {
                            "dateTime": {
                                 "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": [
                            "dateTime"
                        ],
                        "properties": {
                            "dateTime": {
                                 "type": "string"
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/ClassWithLocalDateTime",
                        "definitions": {
                            "ClassWithLocalDateTime": {
                                "type": "object",
                                "required": [
                                    "dateTime"
                                ],
                                "properties": {
                                    "dateTime": {
                                        "type": "string"
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                type = getKType<ClassWithNestedClass>(),
                testName = "auto title",
                generatorModules = listOf(
                    AutoTitleModule(type = AutoTitleType.SIMPLE_NAME)
                ),
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
                                "${'$'}ref": "#/definitions/NestedClass"
                            }
                        },
                        "title": "ClassWithNestedClass",
                        "definitions": {
                            "NestedClass": {
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
                        "${'$'}ref": "#/definitions/ClassWithNestedClass",
                        "definitions": {
                            "NestedClass": {
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
                            "ClassWithNestedClass": {
                                "type": "object",
                                "required": [
                                    "nested"
                                ],
                                "properties": {
                                    "nested": {
                                        "${'$'}ref": "#/definitions/NestedClass"
                                    }
                                },
                                "title": "ClassWithNestedClass"
                            }
                        }
                    }
                """.trimIndent(),
            ),
        )

    }

}