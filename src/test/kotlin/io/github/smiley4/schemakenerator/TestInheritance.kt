package io.github.smiley4.schemakenerator

import io.github.smiley4.schemakenerator.analysis.TypeContext
import io.github.smiley4.schemakenerator.analysis.TypeResolver
import io.github.smiley4.schemakenerator.assertions.ExpectedMemberData
import io.github.smiley4.schemakenerator.assertions.ExpectedTypeData
import io.github.smiley4.schemakenerator.assertions.shouldHave
import io.github.smiley4.schemakenerator.assertions.shouldMatch
import io.github.smiley4.schemakenerator.models.TestSubClass
import io.kotest.core.spec.style.StringSpec

class TestInheritance : StringSpec({

    "test basic" {
        val context = TypeContext()
        TypeResolver(context).resolve<TestSubClass>()
            .let { context.getData(it)!! }
            .also { type ->
                type.shouldMatch(
                    ExpectedTypeData(
                        simpleName = "TestSubClass",
                        supertypeIds = listOf("io.github.smiley4.schemakenerator.models.TestOpenClass"),
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
            "io.github.smiley4.schemakenerator.models.TestSubClass",
            "io.github.smiley4.schemakenerator.models.TestOpenClass",
            "kotlin.String",
            "kotlin.Int",
            "kotlin.Any"
        )
    }

})