package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.assertions.ExpectedObjectTypeData
import io.github.smiley4.schemakenerator.parser.core.TypeParserContext
import io.github.smiley4.schemakenerator.parser.reflection.ReflectionTypeParser
import io.github.smiley4.schemakenerator.assertions.ExpectedPropertyData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeParameterData
import io.github.smiley4.schemakenerator.assertions.shouldHaveExactly
import io.github.smiley4.schemakenerator.assertions.shouldMatch
import io.github.smiley4.schemakenerator.parser.reflection.ReflectionTypeParserConfig
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

})