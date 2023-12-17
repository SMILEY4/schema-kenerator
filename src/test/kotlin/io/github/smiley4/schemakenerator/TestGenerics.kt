package io.github.smiley4.schemakenerator

import io.github.smiley4.schemakenerator.parser.core.TypeParsingContext
import io.github.smiley4.schemakenerator.parser.reflection.TypeReflectionParser
import io.github.smiley4.schemakenerator.assertions.ExpectedMemberData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeParameterData
import io.github.smiley4.schemakenerator.assertions.shouldHave
import io.github.smiley4.schemakenerator.assertions.shouldMatch
import io.github.smiley4.schemakenerator.models.TestClassDeepGeneric
import io.github.smiley4.schemakenerator.models.TestClassGeneric
import io.github.smiley4.schemakenerator.parser.core.TypeParsingConfig
import io.kotest.core.spec.style.StringSpec

class TestGenerics : StringSpec({

    "test basic single generic" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<TestClassGeneric<String>>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestClassGeneric",
                        typeParameters = mapOf(
                            "T" to ExpectedTypeParameterData(
                                name = "T",
                                typeId = "kotlin.String",
                                nullable = false
                            )
                        ),
                        members = listOf(
                            ExpectedMemberData(
                                name = "value",
                                typeId = "kotlin.String"
                            )
                        )
                    )
                )
            }
        context shouldHave listOf(
            "io.github.smiley4.schemakenerator.models.TestClassGeneric<kotlin.String>",
            "kotlin.String",
            "kotlin.Any"
        )
    }

    "test nested same single generic" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<TestClassGeneric<TestClassGeneric<String>>>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestClassGeneric",
                        typeParameters = mapOf(
                            "T" to ExpectedTypeParameterData(
                                name = "T",
                                typeId = "io.github.smiley4.schemakenerator.models.TestClassGeneric<kotlin.String>",
                                nullable = false
                            )
                        ),
                        members = listOf(
                            ExpectedMemberData(
                                name = "value",
                                typeId = "io.github.smiley4.schemakenerator.models.TestClassGeneric<kotlin.String>"
                            )
                        )
                    )
                )
            }
        context shouldHave listOf(
            "io.github.smiley4.schemakenerator.models.TestClassGeneric<io.github.smiley4.schemakenerator.models.TestClassGeneric<kotlin.String>>",
            "io.github.smiley4.schemakenerator.models.TestClassGeneric<kotlin.String>",
            "kotlin.String",
            "kotlin.Any"
        )
    }


    "test deep generic" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<TestClassDeepGeneric<String>>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestClassDeepGeneric",
                        typeParameters = mapOf(
                            "E" to ExpectedTypeParameterData(
                                name = "E",
                                typeId = "kotlin.String",
                                nullable = false
                            )
                        ),
                        members = listOf(
                            ExpectedMemberData(
                                name = "myValues",
                                typeId = "kotlin.collections.List<kotlin.String>"
                            )
                        )
                    )
                )
            }
        context shouldHave listOf(
            "io.github.smiley4.schemakenerator.models.TestClassDeepGeneric<kotlin.String>",
            "kotlin.collections.List<kotlin.String>",
            "kotlin.collections.Collection<kotlin.String>",
            "kotlin.collections.Iterable<kotlin.String>",
            "kotlin.Int",
            "kotlin.Any",
            "kotlin.String",
        )
    }

    "test wildcard" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<TestClassGeneric<*>>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestClassGeneric",
                        typeParameters = mapOf(
                            "T" to ExpectedTypeParameterData(
                                name = "T",
                                typeId = "*",
                                nullable = false
                            )
                        ),
                        members = listOf(
                            ExpectedMemberData(
                                name = "value",
                                typeId = "*"
                            )
                        )
                    )
                )
            }
        context shouldHave listOf(
            "io.github.smiley4.schemakenerator.models.TestClassGeneric<*>",
            "kotlin.Any",
            "*"
        )
    }

})