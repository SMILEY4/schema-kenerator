package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.idStr
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
import io.github.smiley4.schemakenerator.testutils.ExpectedObjectTypeData
import io.github.smiley4.schemakenerator.testutils.ExpectedPropertyData
import io.github.smiley4.schemakenerator.testutils.shouldHaveExactly
import io.github.smiley4.schemakenerator.testutils.shouldMatch
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestInheritance : StringSpec({

    "test basic" {
        val context = TypeParserContext()
        val result = ReflectionTypeParser(context = context, config =  TestGenerics.CONFIG).parse<TestSubClass>()
        context shouldHaveExactly listOf(
            "io.github.smiley4.schemakenerator.reflection.TestSubClass",
            "io.github.smiley4.schemakenerator.reflection.TestOpenClass",
            "kotlin.String",
            "kotlin.Int",
        )
        result.also { ref ->
            (ref is ContextTypeRef) shouldBe true
            ref.idStr() shouldBe "io.github.smiley4.schemakenerator.reflection.TestSubClass"
        }
        result.resolve(context)!!.also { type ->
            type.shouldMatch(TypeDataContext.testSubClass())
        }
    }

})