package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.assertions.ExpectedObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.assertions.ExpectedPropertyData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeParameterData
import io.github.smiley4.schemakenerator.assertions.shouldHaveExactly
import io.github.smiley4.schemakenerator.assertions.shouldMatch
import io.kotest.core.spec.style.StringSpec

class TestGenerics : StringSpec({

    "test basic single generic" {
        val context = TypeParserContext()
        ReflectionTypeParser(context = context).parse<TestClassGeneric<String>>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassGeneric",
                        typeParameters = mapOf(
                            "T" to ExpectedTypeParameterData(
                                name = "T",
                                typeId = "kotlin.String",
                                nullable = false
                            )
                        ),
                        members = listOf(
                            ExpectedPropertyData(
                                name = "value",
                                typeId = "kotlin.String"
                            )
                        )
                    )
                )
            }
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<kotlin.String>",
            "kotlin.String",
            "kotlin.Any"
        )
    }

    "test nested same single generic" {
        val context = TypeParserContext()
        ReflectionTypeParser(context = context).parse<TestClassGeneric<TestClassGeneric<String>>>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassGeneric",
                        typeParameters = mapOf(
                            "T" to ExpectedTypeParameterData(
                                name = "T",
                                typeId = "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<kotlin.String>",
                                nullable = false
                            )
                        ),
                        members = listOf(
                            ExpectedPropertyData(
                                name = "value",
                                typeId = "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<kotlin.String>"
                            )
                        )
                    )
                )
            }
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<io.github.smiley4.schemakenerator.reflection.TestClassGeneric<kotlin.String>>",
            "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<kotlin.String>",
            "kotlin.String",
            "kotlin.Any"
        )
    }


    "test deep generic" {
        val context = TypeParserContext()
        ReflectionTypeParser(context = context).parse<TestClassDeepGeneric<String>>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassDeepGeneric",
                        typeParameters = mapOf(
                            "E" to ExpectedTypeParameterData(
                                name = "E",
                                typeId = "kotlin.String",
                                nullable = false
                            )
                        ),
                        members = listOf(
                            ExpectedPropertyData(
                                name = "myValues",
                                typeId = "kotlin.collections.List<kotlin.String>"
                            )
                        )
                    )
                )
            }
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassDeepGeneric<kotlin.String>",
            "kotlin.collections.List<kotlin.String>",
            "kotlin.collections.Collection<kotlin.String>",
            "kotlin.collections.Iterable<kotlin.String>",
            "kotlin.Int",
            "kotlin.Any",
            "kotlin.String",
        )
    }

    "test wildcard" {
        val context = TypeParserContext()
        ReflectionTypeParser(context = context).parse<TestClassGeneric<*>>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassGeneric",
                        typeParameters = mapOf(
                            "T" to ExpectedTypeParameterData(
                                name = "T",
                                typeId = "*",
                                nullable = false
                            )
                        ),
                        members = listOf(
                            ExpectedPropertyData(
                                name = "value",
                                typeId = "*"
                            )
                        )
                    )
                )
            }
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestClassGeneric<*>",
            "kotlin.Any",
            "*"
        )
    }

})