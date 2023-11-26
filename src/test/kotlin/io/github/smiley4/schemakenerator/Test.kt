package io.github.smiley4.schemakenerator

import io.github.smiley4.schemakenerator.assertions.shouldBeAny
import io.github.smiley4.schemakenerator.assertions.shouldBeBoolean
import io.github.smiley4.schemakenerator.assertions.shouldBeBooleanArray
import io.github.smiley4.schemakenerator.assertions.shouldBeByte
import io.github.smiley4.schemakenerator.assertions.shouldBeByteArray
import io.github.smiley4.schemakenerator.assertions.shouldBeChar
import io.github.smiley4.schemakenerator.assertions.shouldBeCharArray
import io.github.smiley4.schemakenerator.assertions.shouldBeDouble
import io.github.smiley4.schemakenerator.assertions.shouldBeFloat
import io.github.smiley4.schemakenerator.assertions.shouldBeInt
import io.github.smiley4.schemakenerator.assertions.shouldBeLong
import io.github.smiley4.schemakenerator.assertions.shouldBeLongArray
import io.github.smiley4.schemakenerator.assertions.shouldBeShort
import io.github.smiley4.schemakenerator.assertions.shouldBeString
import io.github.smiley4.schemakenerator.assertions.shouldBeStringArray
import io.github.smiley4.schemakenerator.assertions.shouldBeUByte
import io.github.smiley4.schemakenerator.assertions.shouldBeUByteArray
import io.github.smiley4.schemakenerator.assertions.shouldBeUInt
import io.github.smiley4.schemakenerator.assertions.shouldBeULong
import io.github.smiley4.schemakenerator.assertions.shouldBeULongArray
import io.github.smiley4.schemakenerator.assertions.shouldBeUShort
import io.github.smiley4.schemakenerator.assertions.shouldHaveProperty
import io.github.smiley4.schemakenerator.models.TestClassSimple
import io.github.smiley4.schemakenerator.models.TestClassSimpleDataTypes
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class Test : StringSpec({

    "Int" {
        val analyzer = ClassAnalyzer()
        analyzer.analyze<Int>().also { info ->
            info.generics shouldHaveSize 0
            info.simpleName shouldBe "Int"
            info.qualifiedName shouldBe "kotlin.Int"
            info.properties shouldHaveSize 0
        }
    }

    "String" {
        val analyzer = ClassAnalyzer()
        analyzer.analyze<String>().also { info ->
            info.generics shouldHaveSize 0
            info.simpleName shouldBe "String"
            info.qualifiedName shouldBe "kotlin.String"
            info.properties shouldHaveSize 0
        }
    }

    "Any" {
        val analyzer = ClassAnalyzer()
        analyzer.analyze<Any>().also { info ->
            info.simpleName shouldBe "Any"
            info.qualifiedName shouldBe "kotlin.Any"
            info.properties shouldHaveSize 0
        }
    }

    "Unit" {
        val analyzer = ClassAnalyzer()
        analyzer.analyze<Unit>().also { info ->
            info.simpleName shouldBe "Unit"
            info.qualifiedName shouldBe "kotlin.Unit"
            info.properties shouldHaveSize 0
        }
    }

    "TestClassSimple" {
        val analyzer = ClassAnalyzer()
        analyzer.analyze<TestClassSimple>().also { type ->

            type.simpleName shouldBe "TestClassSimple"
            type.qualifiedName shouldBe "io.github.smiley4.schemakenerator.models.TestClassSimple"
            type.generics shouldHaveSize 0
            type.properties shouldHaveSize 1

            type.shouldHaveProperty("someField") { prop ->
                prop.name shouldBe "someField"
                prop.typeInformation.shouldBeString()
                prop.typeInformation.nullable shouldBe false
            }
        }
    }

    "TestClassSimpleDataTypes" {
        val analyzer = ClassAnalyzer()
        analyzer.analyze<TestClassSimpleDataTypes>().also { info ->

            info.simpleName shouldBe "TestClassSimpleDataTypes"
            info.qualifiedName shouldBe "io.github.smiley4.schemakenerator.models.TestClassSimpleDataTypes"
            info.generics shouldHaveSize 0
            info.properties shouldHaveSize 30

            info.shouldHaveProperty("fieldByte") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeByte()
            }

            info.shouldHaveProperty("fieldShort") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeShort()
            }
            
            info.shouldHaveProperty("fieldInt") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeInt()
            }
            
            info.shouldHaveProperty("fieldLong") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeLong()
            }
            
            info.shouldHaveProperty("fieldFloat") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeFloat()
            }
            
            info.shouldHaveProperty("fieldDouble") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeDouble()
            }
            
            info.shouldHaveProperty("fieldUByte") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeUByte()
            }
            
            info.shouldHaveProperty("fieldUShort") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeUShort()
            }
            
            info.shouldHaveProperty("fieldUInt") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeUInt()
            }
            
            info.shouldHaveProperty("fieldULong") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeULong()
            }
            
            info.shouldHaveProperty("fieldUByteArray") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeUByteArray()
            }
            
            info.shouldHaveProperty("fieldByteArray") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeByteArray()
            }
            
            info.shouldHaveProperty("fieldULongArray") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeULongArray()
            }
            
            info.shouldHaveProperty("fieldLongArray") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeLongArray()
            }
            
            info.shouldHaveProperty("fieldBoolean") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeBoolean()
            }
            
            info.shouldHaveProperty("fieldBoolArray") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeBooleanArray()
            }
            
            info.shouldHaveProperty("fieldChar") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeChar()
            }
            
            info.shouldHaveProperty("fieldCharArray") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeCharArray()
            }
            
            info.shouldHaveProperty("fieldString") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeString()
            }
            
            info.shouldHaveProperty("fieldStringArray") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeStringArray()
            }
            
            info.shouldHaveProperty("fieldObject") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.also { propType ->
                    propType.generics shouldHaveSize 0
                    propType.simpleName shouldBe "TestClassSimple"
                    propType.qualifiedName shouldBe "io.github.smiley4.schemakenerator.models.TestClassSimple"
                    propType.properties shouldHaveSize 1
                }
            }

            info.shouldHaveProperty("fieldAny") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.shouldBeAny()
            }
            
            info.shouldHaveProperty("fieldNullableString") { prop ->
                prop.typeInformation.nullable shouldBe true
                prop.typeInformation.shouldBeString()
            }
            
            info.shouldHaveProperty("fieldNullableObject") { prop ->
                prop.typeInformation.nullable shouldBe true
                prop.typeInformation.also { propType ->
                    propType.generics shouldHaveSize 0
                    propType.simpleName shouldBe "TestClassSimple"
                    propType.qualifiedName shouldBe "io.github.smiley4.schemakenerator.models.TestClassSimple"
                    propType.properties shouldHaveSize 1
                }
            }
            
            info.shouldHaveProperty("fieldNullableAny") { prop ->
                prop.typeInformation.nullable shouldBe true
                prop.typeInformation.shouldBeAny()

            }
            
            info.shouldHaveProperty("fieldStringList") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.also { propType ->
                    propType.generics shouldHaveSize 1
                    propType.generics["E"]
                        .also { it.shouldNotBeNull() }
                        ?.also { genericType ->
                            genericType.shouldBeString()
                        }
                    propType.simpleName shouldBe "List"
                    propType.qualifiedName shouldBe "kotlin.collections.List"
                    propType.properties shouldHaveSize 0
                }
            }
            
            info.shouldHaveProperty("fieldObjectList") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.also { propType ->
                    propType.generics shouldHaveSize 1
                    propType.generics["E"]
                        .also { it.shouldNotBeNull() }
                        ?.also { genericType ->
                            genericType.simpleName shouldBe "TestClassSimple"
                        }
                    propType.simpleName shouldBe "List"
                    propType.qualifiedName shouldBe "kotlin.collections.List"
                    propType.properties shouldHaveSize 0
                }
            }
            
            info.shouldHaveProperty("fieldNullableObjectList") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.also { propType ->
                    propType.generics shouldHaveSize 1
                    propType.generics["E"]
                        .also { it.shouldNotBeNull() }
                        ?.also { genericType ->
                            genericType.nullable shouldBe true
                            genericType.simpleName shouldBe "TestClassSimple"
                        }
                    propType.simpleName shouldBe "List"
                    propType.qualifiedName shouldBe "kotlin.collections.List"
                    propType.properties shouldHaveSize 0
                }
            }
            
            info.shouldHaveProperty("fieldPrimitiveMap") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.also { propType ->
                    propType.generics shouldHaveSize 2
                    propType.generics["K"]
                        .also { it.shouldNotBeNull() }
                        ?.also { it.shouldBeString() }
                    propType.generics["V"]
                        .also { it.shouldNotBeNull() }
                        ?.also { it.shouldBeInt()}
                    propType.simpleName shouldBe "Map"
                    propType.qualifiedName shouldBe "kotlin.collections.Map"
                    propType.properties shouldHaveSize 0
                }
            }
            
            info.shouldHaveProperty("fieldComplexMap") { prop ->
                prop.typeInformation.nullable shouldBe false
                prop.typeInformation.also { propType ->
                    propType.generics shouldHaveSize 2
                    propType.generics["K"]
                        .also { it.shouldNotBeNull() }
                        ?.also { genericType ->
                            genericType.simpleName shouldBe "TestClassSimple"
                        }
                    propType.generics["V"]
                        .also { it.shouldNotBeNull() }
                        ?.also { it.shouldBeAny()}
                    propType.simpleName shouldBe "Map"
                    propType.qualifiedName shouldBe "kotlin.collections.Map"
                    propType.properties shouldHaveSize 0
                }
            }
            
        }
    }

})