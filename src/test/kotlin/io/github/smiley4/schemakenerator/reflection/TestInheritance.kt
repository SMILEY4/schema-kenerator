package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.parser.core.TypeParsingContext
import io.github.smiley4.schemakenerator.parser.reflection.TypeReflectionParser
import io.github.smiley4.schemakenerator.assertions.ExpectedMemberData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeData
import io.github.smiley4.schemakenerator.assertions.shouldHave
import io.github.smiley4.schemakenerator.assertions.shouldMatch
import io.github.smiley4.schemakenerator.parser.core.TypeParsingConfig
import io.kotest.core.spec.style.StringSpec

class TestInheritance : StringSpec({

    "test basic" {
        val context = TypeParsingContext()
        TypeReflectionParser(TypeParsingConfig(), context).parse<TestSubClass>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestSubClass",
                        supertypeIds = listOf("io.github.smiley4.schemakenerator.reflection.TestOpenClass"),
                        members = listOf(
                            ExpectedMemberData(
                                name = "additionalField",
                                typeId = "kotlin.Int"
                            )
                        )
                    )
                )
            }
        context shouldHave listOf(
            "io.github.smiley4.schemakenerator.reflection.TestSubClass",
            "io.github.smiley4.schemakenerator.reflection.TestOpenClass",
            "kotlin.String",
            "kotlin.Int",
            "kotlin.Any"
        )
    }

})