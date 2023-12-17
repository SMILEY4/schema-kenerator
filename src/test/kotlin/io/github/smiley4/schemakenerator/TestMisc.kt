package io.github.smiley4.schemakenerator

import io.github.smiley4.schemakenerator.parser.TypeParsingContext
import io.github.smiley4.schemakenerator.parser.reflection.TypeReflectionParser
import io.github.smiley4.schemakenerator.assertions.ExpectedMemberData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeParameterData
import io.github.smiley4.schemakenerator.assertions.shouldHave
import io.github.smiley4.schemakenerator.assertions.shouldMatch
import io.github.smiley4.schemakenerator.models.TestClassRecursiveGeneric
import io.kotest.core.spec.style.StringSpec

class TestMisc : StringSpec({

    "test recursive" {
        val context = TypeParsingContext()
        TypeReflectionParser(context).resolve<TestClassRecursiveGeneric<String>>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
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
                            "io.github.smiley4.schemakenerator.models.TestInterfaceRecursiveGeneric<io.github.smiley4.schemakenerator.models.TestClassRecursiveGeneric<*>>"
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
            "io.github.smiley4.schemakenerator.models.TestClassRecursiveGeneric<kotlin.String>",
            "io.github.smiley4.schemakenerator.models.TestInterfaceRecursiveGeneric<io.github.smiley4.schemakenerator.models.TestClassRecursiveGeneric<*>>",
            "io.github.smiley4.schemakenerator.models.TestClassRecursiveGeneric<*>",
            "kotlin.String",
            "kotlin.Any",
            "*"
        )
    }

})