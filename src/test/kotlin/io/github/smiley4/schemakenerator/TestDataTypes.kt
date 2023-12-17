package io.github.smiley4.schemakenerator

import io.github.smiley4.schemakenerator.analysis.TypeContext
import io.github.smiley4.schemakenerator.analysis.TypeResolver
import io.github.smiley4.schemakenerator.analysis.data.TypeRef
import io.github.smiley4.schemakenerator.assertions.ExpectedMemberData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeData
import io.github.smiley4.schemakenerator.assertions.shouldHave
import io.github.smiley4.schemakenerator.assertions.shouldMatch
import io.github.smiley4.schemakenerator.models.TestClassSimple
import io.github.smiley4.schemakenerator.models.TestClassWithAbstractField
import io.github.smiley4.schemakenerator.models.TestClassWithEnumField
import io.github.smiley4.schemakenerator.models.TestClassWithFunctionField
import io.github.smiley4.schemakenerator.models.TestEnum
import io.kotest.core.spec.style.StringSpec

class TestDataTypes : StringSpec({

    "Int" {
        val context = TypeContext()
        TypeResolver(context).resolve<Int>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "Int",
                        qualifiedName = "kotlin.Int",
                        typeParameters = emptyMap(),
                        supertypeIds = emptyList(),
                        members = emptyList()
                    )
                )
            }
        context shouldHave listOf("kotlin.Int")
    }

    "String" {
        val context = TypeContext()
        TypeResolver(context).resolve<String>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "String",
                        qualifiedName = "kotlin.String",
                        typeParameters = emptyMap(),
                        supertypeIds = emptyList(),
                        members = emptyList()
                    )
                )
            }
        context shouldHave listOf("kotlin.String")
    }

    "Any" {
        val context = TypeContext()
        TypeResolver(context).resolve<Any>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "Any",
                        qualifiedName = "kotlin.Any",
                        typeParameters = emptyMap(),
                        supertypeIds = emptyList(),
                        members = emptyList()
                    )
                )
            }
        context shouldHave listOf("kotlin.Any")
    }

    "Unit" {
        val context = TypeContext()
        TypeResolver(context).resolve<Unit>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "Unit",
                        qualifiedName = "kotlin.Unit",
                        typeParameters = emptyMap(),
                        supertypeIds = emptyList(),
                        members = emptyList()
                    )
                )
            }
        context shouldHave listOf("kotlin.Unit")
    }

    "simple class" {
        val context = TypeContext()
        TypeResolver(context).resolve<TestClassSimple>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestClassSimple",
                        qualifiedName = "io.github.smiley4.schemakenerator.models.TestClassSimple",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf("kotlin.Any"),
                        members = listOf(
                            ExpectedMemberData(
                                name = "someField",
                                typeId = "kotlin.String",
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHave listOf(
            "io.github.smiley4.schemakenerator.models.TestClassSimple",
            "kotlin.String",
            "kotlin.Any"
        )
    }

    "enum" {
        val context = TypeContext()
        TypeResolver(context).resolve<TestClassWithEnumField>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestClassWithEnumField",
                        qualifiedName = "io.github.smiley4.schemakenerator.models.TestClassWithEnumField",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf("kotlin.Any"),
                        members = listOf(
                            ExpectedMemberData(
                                name = "value",
                                typeId = "io.github.smiley4.schemakenerator.models.TestEnum",
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHave listOf(

            "io.github.smiley4.schemakenerator.models.TestClassWithEnumField",
            "io.github.smiley4.schemakenerator.models.TestEnum",

            "kotlin.Enum<io.github.smiley4.schemakenerator.models.TestEnum>",

            "kotlin.enums.EnumEntries<io.github.smiley4.schemakenerator.models.TestEnum>",
            "kotlin.collections.List<io.github.smiley4.schemakenerator.models.TestEnum>",
            "kotlin.collections.Collection<io.github.smiley4.schemakenerator.models.TestEnum>",
            "kotlin.collections.Iterable<io.github.smiley4.schemakenerator.models.TestEnum>",

            "kotlin.String",
            "kotlin.Int",
            "kotlin.Any",
        )

        context.getData(TypeRef("io.github.smiley4.schemakenerator.models.TestEnum"))!!.also { type ->
            type.shouldMatch(
                ExpectedTypeData(
                    simpleName = "TestEnum",
                    enumValues = listOf("RED", "GREEN", "BLUE")
                )
            )
        }
    }

    "function type" {
        val context = TypeContext()
        TypeResolver(context).resolve<TestClassWithFunctionField>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestClassWithFunctionField",
                        qualifiedName = "io.github.smiley4.schemakenerator.models.TestClassWithFunctionField",
                        typeParameters = emptyMap(),
                        supertypeIds = listOf("kotlin.Any"),
                        members = listOf(
                            ExpectedMemberData(
                                name = "value",
                                typeId = "kotlin.Function1<kotlin.Int,kotlin.String>",
                                nullable = false
                            )
                        )
                    )
                )
            }
        context shouldHave listOf(
            "io.github.smiley4.schemakenerator.models.TestClassWithFunctionField",
            "kotlin.Function1<kotlin.Int,kotlin.String>",
            "kotlin.Function<kotlin.String>",
            "kotlin.Int",
            "kotlin.String",
            "kotlin.Any"
        )
    }

})