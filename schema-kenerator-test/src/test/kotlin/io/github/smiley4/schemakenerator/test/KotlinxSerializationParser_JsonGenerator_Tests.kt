package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.obj
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaAutoTitleStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileInlineStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileReferenceRootStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaCompileReferenceStep
import io.github.smiley4.schemakenerator.jsonschema.steps.JsonSchemaGenerationStep
import io.github.smiley4.schemakenerator.serialization.steps.KotlinxSerializationTypeProcessingStep
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassDirectSelfReferencing
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWIthDifferentGenerics
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithCollections
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithDeepGeneric
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithNestedClass
import io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields
import io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass
import io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA
import io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum
import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Suppress("ClassName")
class KotlinxSerializationParser_JsonGenerator_Tests : FunSpec({

    context("generator: inlining") {
        withData(TEST_DATA) { data ->

            val schema = data.type
                .let { KotlinxSerializationTypeProcessingStep().process(it) }
                .let { JsonSchemaGenerationStep().generate(it) }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { JsonSchemaAutoTitleStep(TitleType.SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { JsonSchemaCompileInlineStep().compile(it) }

            schema.json.prettyPrint().shouldEqualJson {
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

            val additionalIds = mutableListOf<String>()

            val schema = data.type
                .let { KotlinxSerializationTypeProcessingStep().process(it) }
                .also { schema ->
                    if (schema.data.id.additionalId != null) {
                        additionalIds.add(schema.data.id.additionalId!!)
                    }
                    schema.supporting.forEach {
                        if (it.id.additionalId != null) {
                            additionalIds.add(it.id.additionalId!!)
                        }
                    }
                }
                .let { JsonSchemaGenerationStep().generate(it) }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { JsonSchemaAutoTitleStep(TitleType.SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { JsonSchemaCompileReferenceStep().compile(it) }
                .also {
                    if (it.definitions.isNotEmpty()) {
                        (it.json as JsonObject).properties["definitions"] = obj {
                            it.definitions.forEach { (k, v) ->
                                k to v
                            }
                        }
                    }
                }

            schema.json.prettyPrint()
                .let {
                    var jsonStr = it
                    additionalIds.forEachIndexed { index, addId ->
                        jsonStr = jsonStr.replace("#$addId", "#$index")
                    }
                    jsonStr
                }
                .shouldEqualJson {
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

            val additionalIds = mutableListOf<String>()

            val schema = data.type
                .let { KotlinxSerializationTypeProcessingStep().process(it) }
                .also { schema ->
                    if (schema.data.id.additionalId != null) {
                        additionalIds.add(schema.data.id.additionalId!!)
                    }
                    schema.supporting.forEach {
                        if (it.id.additionalId != null) {
                            additionalIds.add(it.id.additionalId!!)
                        }
                    }
                }
                .let { JsonSchemaGenerationStep().generate(it) }
                .let { list ->
                    if (data.withAutoTitle) {
                        list
                            .let { JsonSchemaAutoTitleStep(TitleType.SIMPLE).process(it) }
                    } else {
                        list
                    }
                }
                .let { JsonSchemaCompileReferenceRootStep().compile(it) }
                .also {
                    if (it.definitions.isNotEmpty()) {
                        (it.json as JsonObject).properties["definitions"] = obj {
                            it.definitions.forEach { (k, v) ->
                                k to v
                            }
                        }
                    }
                }

            schema.json.prettyPrint()
                .let {
                    var jsonStr = it
                    additionalIds.forEachIndexed { index, addId ->
                        jsonStr = jsonStr.replace("#$addId", "#$index")
                    }
                    jsonStr
                }
                .shouldEqualJson {
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
                // top-level lists not directly supported: weird result
                type = typeOf<List<String>>(),
                testName = "list of strings",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "required": [],
                        "properties": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": [],
                        "properties": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/kotlinx.serialization.Polymorphic<List>",
                        "definitions": {
                            "kotlinx.serialization.Polymorphic<List>": {
                                "type": "object",
                                "required": [],
                                "properties": {}
                            }
                        }
                    }
                """.trimIndent(),
            ),
            // top-level maps not directly supported: weird result
            TestData(
                type = typeOf<Map<String, Int>>(),
                testName = "map of strings to integers",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "required": [],
                        "properties": {}
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": [],
                        "properties": {}
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/kotlinx.serialization.Polymorphic<Map>",
                        "definitions": {
                            "kotlinx.serialization.Polymorphic<Map>": {
                                "type": "object",
                                "required": [],
                                "properties": {}
                            }
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
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithSimpleFields": {
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
                    "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum",
                    "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.kotlinx.TestEnum": {
                            "enum": [ "ONE", "TWO", "THREE" ]
                        }
                    }
                }
                """.trimIndent(),
            ),
            TestData(
                // generics not supported with kotlinx-serialization -> fallback to "any"-schema
                type = typeOf<ClassWithGenericField<String>>(),
                testName = "class with defined generic field",
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
                // generics not supported with kotlinx-serialization -> fallback to "any"-schema
                type = typeOf<ClassWithGenericField<*>>(),
                testName = "class with wildcard generic field",
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
                // generics not supported with kotlinx-serialization -> fallback to "any"-schema
                type = typeOf<ClassWithDeepGeneric<String>>(),
                testName = "class with deep generic field",
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
                                "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA"
                            },
                            {
                                "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB"
                            }
                        ],
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA": {
                                "type": "object",
                                "required": ["a", "sealedValue"],
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
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB": {
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
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SealedClass": {
                                "anyOf": [
                                    {
                                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA"
                                    },
                                    {
                                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB"
                                    }
                                ]
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA": {
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
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassB": {
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
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.SubClassA": {
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
                type = typeOf<ClassWIthDifferentGenerics>(),
                testName = "class with multiple different generics",
                expectedResultInlining = """
                    {
                        "type": "object",
                        "required": [
                            "valueInt",
                            "valueString"
                        ],
                        "properties": {
                            "valueInt": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    }
                                }
                            },
                            "valueString": {
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
                expectedResultReferencing = """
                    {
                        "type": "object",
                        "required": [
                            "valueInt",
                            "valueString"
                        ],
                        "properties": {
                            "valueInt": {
                                "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField"
                            },
                            "valueString": {
                                "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField#0"
                            }
                        },
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField#0": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "string"
                                    }
                                }
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWIthDifferentGenerics",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "integer",
                                        "minimum": -2147483648,
                                        "maximum": 2147483647
                                    }
                                }
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField#0": {
                                "type": "object",
                                "required": [
                                    "value"
                                ],
                                "properties": {
                                    "value": {
                                        "type": "string"
                                    }
                                }
                            },
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWIthDifferentGenerics": {
                                "type": "object",
                                "required": [
                                    "valueInt",
                                    "valueString"
                                ],
                                "properties": {
                                    "valueInt": {
                                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField"
                                    },
                                    "valueString": {
                                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithGenericField#0"
                                    }
                                }
                            }
                        }
                    }
                """.trimIndent(),
            ),
//            TestData(
//                type = typeOf<ClassWithLocalDateTime>(),
//                testName = "class with java local-date-time and custom parser",
//                customParsers = mapOf(
//                    LocalDateTime::class to CustomKotlinxSerializationTypeParser { typeId, _ ->
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
//                        "type": "object",
//                        "required": [
//                            "dateTime"
//                        ],
//                        "properties": {
//                            "dateTime": {
//                                 "type": "string"
//                            }
//                        }
//                    }
//                """.trimIndent(),
//                expectedResultReferencing = """
//                    {
//                        "type": "object",
//                        "required": [
//                            "dateTime"
//                        ],
//                        "properties": {
//                            "dateTime": {
//                                 "type": "string"
//                            }
//                        }
//                    }
//                """.trimIndent(),
//                expectedResultReferencingRoot = """
//                    {
//                        "${'$'}ref": "#/definitions/ClassWithLocalDateTime",
//                        "definitions": {
//                            "ClassWithLocalDateTime": {
//                                "type": "object",
//                                "required": [
//                                    "dateTime"
//                                ],
//                                "properties": {
//                                    "dateTime": {
//                                         "type": "string"
//                                    }
//                                }
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
                                "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.NestedClass"
                            }
                        },
                        "title": "ClassWithNestedClass",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.NestedClass": {
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
                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithNestedClass",
                        "definitions": {
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.NestedClass": {
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
                            "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithNestedClass": {
                                "type": "object",
                                "required": [
                                    "nested"
                                ],
                                "properties": {
                                    "nested": {
                                        "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.NestedClass"
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
                        "someList",
                        "someSet",
                        "someMap",
                        "someArray"
                      ],
                      "properties": {
                        "someList": {
                          "type": "array",
                          "items": {
                            "type": "string"
                          }
                        },
                        "someSet": {
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
                        "someArray": {
                          "type": "array",
                          "items": {
                            "type": "integer",
                            "minimum": -2147483648,
                            "maximum": 2147483647
                          }
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencing = """
                    {
                      "type": "object",
                      "required": [
                        "someList",
                        "someSet",
                        "someMap",
                        "someArray"
                      ],
                      "properties": {
                        "someList": {
                          "type": "array",
                          "items": {
                            "type": "string"
                          }
                        },
                        "someSet": {
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
                        "someArray": {
                          "type": "array",
                          "items": {
                            "type": "integer",
                            "minimum": -2147483648,
                            "maximum": 2147483647
                          }
                        }
                      }
                    }
                """.trimIndent(),
                expectedResultReferencingRoot = """
                    {
                      "${'$'}ref": "#/definitions/io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithCollections",
                      "definitions": {
                        "io.github.smiley4.schemakenerator.test.models.kotlinx.ClassWithCollections": {
                          "type": "object",
                          "required": [
                            "someList",
                            "someSet",
                            "someMap",
                            "someArray"
                          ],
                          "properties": {
                            "someList": {
                              "type": "array",
                              "items": {
                                "type": "string"
                              }
                            },
                            "someSet": {
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
                            "someArray": {
                              "type": "array",
                              "items": {
                                "type": "integer",
                                "minimum": -2147483648,
                                "maximum": 2147483647
                              }
                            }
                          }
                        }
                      }
                    }
                """.trimIndent(),
            ),
//            todo: fix infinite loop
//            TestData(
//                type = typeOf<ClassDirectSelfReferencing>(),
//                testName = "class with direct self reference",
//                expectedResultInlining = """
//                    {
//                    }
//                """.trimIndent(),
//                expectedResultReferencing = """
//                    {
//                    }
//                """.trimIndent(),
//                expectedResultReferencingRoot = """
//                    {
//                    }
//                """.trimIndent(),
//            ),
        )
    }

}