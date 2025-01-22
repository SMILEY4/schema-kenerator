package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.annotations.Description
import io.github.smiley4.schemakenerator.core.gettersToProperties
import io.github.smiley4.schemakenerator.core.typedata.MemberKind
import io.github.smiley4.schemakenerator.core.typedata.Visibility
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.reflect.typeOf

class MergeGettersTest : StringSpec({

    "merge getter" {

        val result = typeOf<MyTestClass>()
            .processReflection {
                includeGetters = true
                includeHidden = true
            }
            .gettersToProperties()
            .data

        result.members.also { members ->

            members shouldHaveSize 3

            members.find { it.name == "myNumber" }.also { myNumber ->
                myNumber shouldNotBe null
            }

            members.find { it.name == "someValue" }.also { someValue ->
                someValue shouldNotBe null
                someValue!!.nullable shouldBe false
                someValue.visibility shouldBe Visibility.PUBLIC
                someValue.kind shouldBe MemberKind.PROPERTY
                someValue.annotations shouldHaveSize 1
            }

            members.find { it.name == "flagged" }.also { flagged ->
                flagged shouldNotBe null
                flagged!!.nullable shouldBe false
                flagged.visibility shouldBe Visibility.PUBLIC
                flagged.kind shouldBe MemberKind.PROPERTY
                flagged.annotations shouldHaveSize 0
            }
        }

    }

}) {

    companion object {

        private class MyTestClass(
            val myNumber: Int,
            @Description("some test value")
            private val someValue: String
        ) {

            fun getSomeValue(): String = someValue

            fun isFlagged() = true

        }

    }

}