package io.github.smiley4.schemakenerator

import io.github.smiley4.schemakenerator.assertions.shouldBeInt
import io.github.smiley4.schemakenerator.assertions.shouldBeString
import io.github.smiley4.schemakenerator.assertions.shouldHaveProperty
import io.github.smiley4.schemakenerator.models.TestClassDeepGeneric
import io.github.smiley4.schemakenerator.models.TestClassGeneric
import io.github.smiley4.schemakenerator.models.TestSubClass
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class TestInheritance : StringSpec({

    "test basic" {
        val analyzer = ClassAnalyzer()
        analyzer.analyze<TestSubClass>().also { info ->
            info.simpleName shouldBe "TestSubClass"
            info.qualifiedName shouldBe "io.github.smiley4.schemakenerator.models.TestSubClass"
            info.generics shouldHaveSize 0
            info.properties shouldHaveSize 1
            info.shouldHaveProperty("additionalField") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeInt()
            }
            info.superTypes shouldHaveSize 1
            info.superTypes[0].also { superInfo ->

                superInfo.simpleName shouldBe "TestOpenClass"
                superInfo.qualifiedName shouldBe "io.github.smiley4.schemakenerator.models.TestOpenClass"
                superInfo.generics shouldHaveSize 0
                superInfo.properties shouldHaveSize 1
                superInfo.shouldHaveProperty("baseField") { prop ->
                    prop.typeInformation.nullable shouldBe false
                    prop.typeInformation.shouldBeString()
                }
                superInfo.superTypes shouldHaveSize 0
            }
        }
    }

})