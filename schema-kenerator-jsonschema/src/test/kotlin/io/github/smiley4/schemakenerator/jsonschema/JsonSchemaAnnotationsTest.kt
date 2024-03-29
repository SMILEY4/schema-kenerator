package io.github.smiley4.schemakenerator.jsonschema

import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.Visibility
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import io.github.smiley4.schemakenerator.jsonschema.module.AnnotationJsonSchemaGeneratorModule
import io.github.smiley4.schemakenerator.jsonschema.module.AnnotationJsonSchemaGeneratorModule.Companion.AutoTitle
import io.github.smiley4.schemakenerator.jsonschema.module.StandardJsonSchemaGeneratorModule
import io.kotest.assertions.json.ArrayOrder
import io.kotest.assertions.json.FieldComparison
import io.kotest.assertions.json.NumberFormat
import io.kotest.assertions.json.PropertyOrder
import io.kotest.assertions.json.TypeCoercion
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData

class JsonSchemaAnnotationsTest : FunSpec({

    context("json schema generator: basic inline types") {
        withData(TEST_DATA) { data ->
            val schema = JsonSchemaGenerator()
                .withModule(StandardJsonSchemaGeneratorModule())
                .withModule(AnnotationJsonSchemaGeneratorModule(AutoTitle.SIMPLE_NAME))
                .generate(data.typeData, TypeDataContext())
            schema.asJson().prettyPrint().shouldEqualJson {
                propertyOrder = PropertyOrder.Lenient
                arrayOrder = ArrayOrder.Lenient
                fieldComparison = FieldComparison.Strict
                numberFormat = NumberFormat.Lenient
                typeCoercion = TypeCoercion.Disabled
                data.expectedSchema
            }
        }
    }

}) {
    companion object {

        private class TestData(
            val typeData: InlineTypeRef,
            val expectedSchema: String,
            val testName: String
        ) : WithDataTestName {
            override fun dataTestName() = testName
        }

        private val TEST_DATA = listOf(
            TestData(
                testName = "primitive ubyte",
                typeData = InlineTypeRef(
                    PrimitiveTypeData(
                        id = TypeId("kotlin.UByte"),
                        qualifiedName = "kotlin.UByte",
                        simpleName = "UByte",
                        typeParameters = mutableMapOf()
                    )
                ),
                expectedSchema = """
                    {
                        "title": "UByte",
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 255
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "list",
                typeData = InlineTypeRef(
                    CollectionTypeData(
                        id = TypeId("kotlin.collections.List<kotlin.String>"),
                        qualifiedName = "kotlin.collections.List",
                        simpleName = "List",
                        typeParameters = mutableMapOf(
                            "E" to TypeParameterData(
                                name = "E",
                                type = InlineTypeRef(
                                    PrimitiveTypeData(
                                        id = TypeId("kotlin.String"),
                                        qualifiedName = "kotlin.String",
                                        simpleName = "String",
                                        typeParameters = mutableMapOf()
                                    )
                                ),
                                nullable = false
                            )
                        ),
                        itemType = PropertyData(
                            name = "item",
                            nullable = false,
                            visibility = Visibility.PUBLIC,
                            kind = PropertyType.PROPERTY,
                            type = InlineTypeRef(
                                PrimitiveTypeData(
                                    id = TypeId("kotlin.String"),
                                    qualifiedName = "kotlin.String",
                                    simpleName = "String",
                                    typeParameters = mutableMapOf()
                                )
                            ),
                            annotations = emptyList()
                        ),
                    )
                ),
                expectedSchema = """
                    {
                        "title": "List<String>",
                        "type": "array",
                        "items": {
                            "type": "string",
                            "title": "String"
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "simple class",
                typeData = InlineTypeRef(
                    ObjectTypeData(
                        id = TypeId("MyClass"),
                        qualifiedName = "MyClass",
                        simpleName = "MyClass",
                        typeParameters = mutableMapOf(),
                        members = mutableListOf(
                            PropertyData(
                                name = "requiredField",
                                nullable = false,
                                visibility = Visibility.PUBLIC,
                                kind = PropertyType.PROPERTY,
                                type = InlineTypeRef(
                                    PrimitiveTypeData(
                                        id = TypeId("kotlin.String"),
                                        simpleName = "String",
                                        qualifiedName = "kotlin.String",
                                        typeParameters = mutableMapOf()
                                    )
                                )
                            ),
                            PropertyData(
                                name = "optionalField",
                                nullable = true,
                                visibility = Visibility.PUBLIC,
                                kind = PropertyType.PROPERTY,
                                type = InlineTypeRef(
                                    PrimitiveTypeData(
                                        id = TypeId("kotlin.Boolean"),
                                        simpleName = "Boolean",
                                        qualifiedName = "kotlin.Boolean",
                                        typeParameters = mutableMapOf()
                                    )
                                )
                            )
                        )
                    )
                ),
                expectedSchema = """
                    {
                        "title": "MyClass",
                        "type": "object",
                        "required": ["requiredField"],
                        "properties": {
                            "requiredField": {
                                "title": "String",
                                "type": "string"
                            },
                            "optionalField": {
                                "title": "Boolean",
                                "type": "boolean"
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "generic class with wildcard type",
                typeData = InlineTypeRef(
                    ObjectTypeData(
                        id = TypeId("MyClass"),
                        qualifiedName = "MyClass",
                        simpleName = "MyClass",
                        typeParameters = mutableMapOf(
                            "T" to TypeParameterData(
                                name = "T",
                                nullable = false,
                                type = InlineTypeRef(
                                    WildcardTypeData()
                                )
                            ),
                        ),
                        members = mutableListOf(
                            PropertyData(
                                name = "value",
                                nullable = false,
                                visibility = Visibility.PUBLIC,
                                kind = PropertyType.PROPERTY,
                                type = InlineTypeRef(
                                    WildcardTypeData()
                                )
                            )
                        )
                    )
                ),
                expectedSchema = """
                    {
                        "title": "MyClass<*>",
                        "type": "object",
                        "required": ["value"],
                        "properties": {
                            "value": {
                                "type": "object",
                                "title": "*"
                            }
                        }
                    }
                """.trimIndent(),
            ),
            TestData(
                testName = "enum",
                typeData = InlineTypeRef(
                    EnumTypeData(
                        id = TypeId("TestEnum"),
                        qualifiedName = "TestEnum",
                        simpleName = "TestEnum",
                        typeParameters = mutableMapOf(),
                        enumConstants = mutableListOf("RED", "GREEN", "BLUE")
                    )
                ),
                expectedSchema = """
                    {
                        "title": "TestEnum",
                        "enum": ["RED", "GREEN", "BLUE"]
                    }
                """.trimIndent(),
            ),
        )

    }
}