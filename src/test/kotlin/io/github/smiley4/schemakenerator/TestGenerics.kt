package io.github.smiley4.schemakenerator

import io.github.smiley4.schemakenerator.assertions.shouldBeString
import io.github.smiley4.schemakenerator.assertions.shouldHaveProperty
import io.github.smiley4.schemakenerator.models.TestClassDeepGeneric
import io.github.smiley4.schemakenerator.models.TestClassGeneric
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class TestGenerics : StringSpec({

    "test basic single generic" {
        val analyzer = ClassAnalyzer()
        analyzer.analyze<TestClassGeneric<String>>().also { info ->
            info.simpleName shouldBe "TestClassGeneric"
            info.qualifiedName shouldBe "io.github.smiley4.schemakenerator.models.TestClassGeneric"
            info.generics shouldHaveSize 1
            info.generics["T"]
                .also { it.shouldNotBeNull() }
                ?.also { genericType ->
                    genericType.shouldBeString()
                }
            info.properties shouldHaveSize 1
            info.shouldHaveProperty("value") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeString()
            }
        }
    }

    "test nested same single generic" {
        val analyzer = ClassAnalyzer()
        analyzer.analyze<TestClassGeneric<TestClassGeneric<String>>>().also { info ->
            info.simpleName shouldBe "TestClassGeneric"
            info.qualifiedName shouldBe "io.github.smiley4.schemakenerator.models.TestClassGeneric"
            info.generics shouldHaveSize 1
            info.generics["T"]
                .also { it.shouldNotBeNull() }
                ?.also { genericType1 ->
                    genericType1.simpleName shouldBe "TestClassGeneric"
                    genericType1.qualifiedName shouldBe "io.github.smiley4.schemakenerator.models.TestClassGeneric"
                    genericType1.generics shouldHaveSize 1
                    genericType1.generics["T"]
                        .also { it.shouldNotBeNull() }
                        ?.also { genericType2 ->
                            genericType2.shouldBeString()
                        }
                    genericType1.properties shouldHaveSize 1
                    genericType1.shouldHaveProperty("value") { prop2 ->
                        prop2.typeInformation.nullable shouldBe false
                        prop2.typeInformation.shouldBeString()
                    }
                }
            info.properties shouldHaveSize 1
            info.shouldHaveProperty("value") { prop1 ->
                prop1.typeInformation.simpleName shouldBe "TestClassGeneric"
                prop1.typeInformation.qualifiedName shouldBe "io.github.smiley4.schemakenerator.models.TestClassGeneric"
                prop1.typeInformation.generics shouldHaveSize 1
                prop1.typeInformation.generics["T"]
                    .also { it.shouldNotBeNull() }
                    ?.also { genericType ->
                        genericType.shouldBeString()
                    }
                prop1.typeInformation.properties shouldHaveSize 1
                prop1.typeInformation.shouldHaveProperty("value") { prop2 ->
                    prop2.typeInformation.nullable shouldBe false
                    prop2.typeInformation.shouldBeString()
                }

            }
        }
    }

    "test nested list" {
        val analyzer = ClassAnalyzer()
        analyzer.analyze<TestClassGeneric<List<String>>>().also { info ->
            info.simpleName shouldBe "TestClassGeneric"
            info.qualifiedName shouldBe "io.github.smiley4.schemakenerator.models.TestClassGeneric"
            info.generics shouldHaveSize 1
            info.generics["T"]
                .also { it.shouldNotBeNull() }
                ?.also { genericType1 ->
                    genericType1.simpleName shouldBe "List"
                    genericType1.qualifiedName shouldBe "kotlin.collections.List"
                    genericType1.generics shouldHaveSize 1
                    genericType1.generics["E"]
                        .also { it.shouldNotBeNull() }
                        ?.also { genericType2 ->
                            genericType2.shouldBeString()
                        }
                    genericType1.properties shouldHaveSize 1
                    genericType1.shouldHaveProperty("value") { prop2 ->
                        prop2.typeInformation.nullable shouldBe false
                        prop2.typeInformation.shouldBeString()
                    }
                }
            info.properties shouldHaveSize 1
            info.shouldHaveProperty("value") { prop1 ->
                prop1.typeInformation.simpleName shouldBe "List"
                prop1.typeInformation.qualifiedName shouldBe "kotlin.collections.List"
                prop1.typeInformation.generics shouldHaveSize 1
                prop1.typeInformation.generics["E"]
                    .also { it.shouldNotBeNull() }
                    ?.also { genericType ->
                        genericType.shouldBeString()
                    }
                prop1.typeInformation.properties shouldHaveSize 0
            }
        }
    }

    "test deep generic" {
        val analyzer = ClassAnalyzer()
        analyzer.analyze<TestClassDeepGeneric<String>>().also { info ->
            info.simpleName shouldBe "TestClassDeepGeneric"
            info.qualifiedName shouldBe "io.github.smiley4.schemakenerator.models.TestClassDeepGeneric"
            info.generics shouldHaveSize 1
            info.generics["T"]
                .also { it.shouldNotBeNull() }
                ?.also { genericType ->
                    genericType.shouldBeString()
                }
            info.properties shouldHaveSize 1
            info.shouldHaveProperty("values") { prop ->
                prop.typeInformation.simpleName shouldBe "List"
                prop.typeInformation.qualifiedName shouldBe "kotlin.collections.List"
                prop.typeInformation.generics shouldHaveSize 1
                prop.typeInformation.generics["E"]
                    .also { it.shouldNotBeNull() }
                    ?.also { genericType ->
                        genericType.shouldBeString()
                    }
                prop.typeInformation.properties shouldHaveSize 0
            }
        }
    }
})