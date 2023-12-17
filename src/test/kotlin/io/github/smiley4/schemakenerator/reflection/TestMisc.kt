package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.parser.core.TypeParsingContext
import io.github.smiley4.schemakenerator.parser.reflection.TypeReflectionParser
import io.github.smiley4.schemakenerator.assertions.ExpectedMemberData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeParameterData
import io.github.smiley4.schemakenerator.assertions.shouldHave
import io.github.smiley4.schemakenerator.assertions.shouldMatch
import io.github.smiley4.schemakenerator.parser.core.TypeParsingConfig
import io.kotest.core.spec.style.StringSpec

class TestMisc : StringSpec({

    "test recursive" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<TestClassRecursiveGeneric<String>>()
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
                            "io.github.smiley4.schemakenerator.reflection.TestInterfaceRecursiveGeneric<io.github.smiley4.schemakenerator.models.TestClassRecursiveGeneric<*>>"
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
            "io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<kotlin.String>",
            "io.github.smiley4.schemakenerator.reflection.TestInterfaceRecursiveGeneric<io.github.smiley4.schemakenerator.models.TestClassRecursiveGeneric<*>>",
            "io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<*>",
            "kotlin.String",
            "kotlin.Any",
            "*"
        )
    }

})