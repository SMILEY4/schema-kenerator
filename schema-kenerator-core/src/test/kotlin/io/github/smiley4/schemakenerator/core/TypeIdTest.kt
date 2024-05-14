package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.TypeId
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class TypeIdTest : FunSpec({

    context("typeId tests") {
        withData(TEST_DATA) { fullIdString ->
            TypeId.parse(fullIdString).full() shouldBe fullIdString
        }
    }

}) {

    companion object {

        val TEST_DATA = listOf(
            "test.MyTest",
            "test.MyTest#1234",
            "test.MyTest<hello>",
            "test.MyTest<hello>#1234",
            "test.MyTest<hello,world>",
            "test.MyTest<hello,world>#1234",
            "test.MyTest<hello,world<42,720>>"
        )

    }

}
