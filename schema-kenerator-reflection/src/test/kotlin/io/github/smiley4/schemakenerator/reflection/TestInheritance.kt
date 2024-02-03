package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.testutils.ExpectedObjectTypeData
import io.github.smiley4.schemakenerator.testutils.ExpectedPropertyData
import io.github.smiley4.schemakenerator.testutils.shouldHaveExactly
import io.github.smiley4.schemakenerator.testutils.shouldMatch
import io.kotest.core.spec.style.StringSpec

class TestInheritance : StringSpec({

//    "test basic" {
//        val context = TypeParserContext()
//        ReflectionTypeParser(context = context).parse<TestSubClass>()
//            .let { context.getData(it)!! }
//            .also { type ->
//                type.shouldMatch(
//                    ExpectedObjectTypeData(
//                        simpleName = "TestSubClass",
//                        supertypeIds = listOf("io.github.smiley4.schemakenerator.reflection.TestOpenClass"),
//                        members = listOf(
//                            ExpectedPropertyData(
//                                name = "additionalField",
//                                typeId = "kotlin.Int"
//                            )
//                        )
//                    )
//                )
//            }
//        context shouldHaveExactly listOf(
//            "io.github.smiley4.schemakenerator.reflection.TestSubClass",
//            "io.github.smiley4.schemakenerator.reflection.TestOpenClass",
//            "kotlin.String",
//            "kotlin.Int",
//            "kotlin.Any"
//        )
//    }

})