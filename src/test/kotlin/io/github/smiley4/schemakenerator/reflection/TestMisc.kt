package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.assertions.ExpectedObjectTypeData
import io.github.smiley4.schemakenerator.parser.core.TypeParserContext
import io.github.smiley4.schemakenerator.parser.reflection.ReflectionTypeParser
import io.github.smiley4.schemakenerator.assertions.ExpectedPropertyData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeParameterData
import io.github.smiley4.schemakenerator.assertions.shouldHaveExactly
import io.github.smiley4.schemakenerator.assertions.shouldMatch
import io.kotest.core.spec.style.StringSpec

class TestMisc : StringSpec({

    "test recursive" {
        val context = TypeParserContext()
        ReflectionTypeParser(context = context).parse<TestClassRecursiveGeneric<String>>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassRecursiveGeneric",
                        typeParameters = mapOf(
                            "T" to ExpectedTypeParameterData(
                                name = "T",
                                typeId = "kotlin.String",
                                nullable = false
                            )
                        ),
                        supertypeIds = listOf(
                            "kotlin.Any",
                            "io.github.smiley4.schemakenerator.reflection.TestInterfaceRecursiveGeneric<io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<*>>"
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
            "io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<kotlin.String>",
            "io.github.smiley4.schemakenerator.reflection.TestInterfaceRecursiveGeneric<io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<*>>",
            "io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<*>",
            "kotlin.String",
            "kotlin.Any",
            "*"
        )
    }

    "test all functions" {
        val context = TypeParserContext()
        ReflectionTypeParser(
            context = context,
            config = {
                includeAllFunctions = true
                includeWeakGetters = false
                includeGetters = false
                includeHidden = false
            }
        ).parse<TestClassWithMethods>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassWithMethods",
                        members = listOf(
                            ExpectedPropertyData(
                                name = "someText",
                                typeId = "kotlin.String"
                            ),
                            ExpectedPropertyData(
                                name = "myFlag",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "isEnabled",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "calculateValue",
                                typeId = "kotlin.Int"
                            ),
                            ExpectedPropertyData(
                                name = "compare",
                                typeId = "kotlin.Long"
                            ),
                            ExpectedPropertyData(
                                name = "isDisabled",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "equals",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "hashCode",
                                typeId = "kotlin.Int"
                            ),
                            ExpectedPropertyData(
                                name = "toString",
                                typeId = "kotlin.String"
                            ),
                        )
                    )
                )
            }
    }

    "test all functions, including hidden" {
        val context = TypeParserContext()
        ReflectionTypeParser(
            context = context,
            config = {
                includeAllFunctions = true
                includeWeakGetters = false
                includeGetters = false
                includeHidden = true
            }
        ).parse<TestClassWithMethods>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassWithMethods",
                        members = listOf(
                            ExpectedPropertyData(
                                name = "someText",
                                typeId = "kotlin.String"
                            ),
                            ExpectedPropertyData(
                                name = "myFlag",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "isEnabled",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "calculateValue",
                                typeId = "kotlin.Int"
                            ),
                            ExpectedPropertyData(
                                name = "compare",
                                typeId = "kotlin.Long"
                            ),
                            ExpectedPropertyData(
                                name = "isDisabled",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "equals",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "hashCode",
                                typeId = "kotlin.Int"
                            ),
                            ExpectedPropertyData(
                                name = "toString",
                                typeId = "kotlin.String"
                            ),
                            ExpectedPropertyData(
                                name = "hiddenField",
                                typeId = "kotlin.String"
                            ),
                            ExpectedPropertyData(
                                name = "hiddenFunction",
                                typeId = "kotlin.String"
                            ),
                        )
                    )
                )
            }
    }

    "test 'all' getter functions" {
        val context = TypeParserContext()
        ReflectionTypeParser(
            context = context,
            config = {
                includeAllFunctions = false
                includeWeakGetters = true
                includeGetters = false
                includeHidden = false
            }
        ).parse<TestClassWithMethods>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassWithMethods",
                        members = listOf(
                            ExpectedPropertyData(
                                name = "someText",
                                typeId = "kotlin.String"
                            ),
                            ExpectedPropertyData(
                                name = "myFlag",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "isEnabled",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "calculateValue",
                                typeId = "kotlin.Int"
                            ),
                            ExpectedPropertyData(
                                name = "isDisabled",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "hashCode",
                                typeId = "kotlin.Int"
                            ),
                            ExpectedPropertyData(
                                name = "toString",
                                typeId = "kotlin.String"
                            ),
                        )
                    )
                )
            }
    }

    "test 'true' getter functions" {
        val context = TypeParserContext()
        ReflectionTypeParser(
            context = context,
            config = {
                includeAllFunctions = false
                includeWeakGetters = false
                includeGetters = true
                includeHidden = false
            }
        ).parse<TestClassWithMethods>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedObjectTypeData(
                        simpleName = "TestClassWithMethods",
                        members = listOf(
                            ExpectedPropertyData(
                                name = "someText",
                                typeId = "kotlin.String"
                            ),
                            ExpectedPropertyData(
                                name = "myFlag",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "isEnabled",
                                typeId = "kotlin.Boolean"
                            ),
                            ExpectedPropertyData(
                                name = "isDisabled",
                                typeId = "kotlin.Boolean"
                            ),
                        )
                    )
                )
            }
    }

})