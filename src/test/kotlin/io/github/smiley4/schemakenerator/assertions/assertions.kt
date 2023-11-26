package io.github.smiley4.schemakenerator.assertions

import io.github.smiley4.schemakenerator.data.PropertyInformation
import io.github.smiley4.schemakenerator.data.TypeInformation
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

fun TypeInformation.shouldHaveProperty(name: String, block: (prop: PropertyInformation) -> Unit) {
    this.properties.find { it.name == name }
        .also { it.shouldNotBeNull() }
        ?.also { block(it) }
}

fun TypeInformation.shouldBeAny() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "Any"
    this.qualifiedName shouldBe "kotlin.Any"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeByte() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "Byte"
    this.qualifiedName shouldBe "kotlin.Byte"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeShort() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "Short"
    this.qualifiedName shouldBe "kotlin.Short"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeInt() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "Int"
    this.qualifiedName shouldBe "kotlin.Int"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeLong() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "Long"
    this.qualifiedName shouldBe "kotlin.Long"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeFloat() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "Float"
    this.qualifiedName shouldBe "kotlin.Float"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeDouble() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "Double"
    this.qualifiedName shouldBe "kotlin.Double"
    this.properties shouldHaveSize 0
}


fun TypeInformation.shouldBeUByte() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "UByte"
    this.qualifiedName shouldBe "kotlin.UByte"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeUShort() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "UShort"
    this.qualifiedName shouldBe "kotlin.UShort"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeUInt() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "UInt"
    this.qualifiedName shouldBe "kotlin.UInt"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeULong() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "ULong"
    this.qualifiedName shouldBe "kotlin.ULong"
    this.properties shouldHaveSize 0
}


fun TypeInformation.shouldBeUByteArray() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "UByteArray"
    this.qualifiedName shouldBe "kotlin.UByteArray"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeByteArray() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "ByteArray"
    this.qualifiedName shouldBe "kotlin.ByteArray"
    this.properties shouldHaveSize 0
}


fun TypeInformation.shouldBeULongArray() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "ULongArray"
    this.qualifiedName shouldBe "kotlin.ULongArray"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeLongArray() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "LongArray"
    this.qualifiedName shouldBe "kotlin.LongArray"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeBoolean() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "Boolean"
    this.qualifiedName shouldBe "kotlin.Boolean"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeBooleanArray() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "BooleanArray"
    this.qualifiedName shouldBe "kotlin.BooleanArray"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeChar() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "Char"
    this.qualifiedName shouldBe "kotlin.Char"
    this.properties shouldHaveSize 0
}

fun TypeInformation.shouldBeCharArray() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "CharArray"
    this.qualifiedName shouldBe "kotlin.CharArray"
    this.properties shouldHaveSize 0
}


fun TypeInformation.shouldBeString() {
    this.generics shouldHaveSize 0
    this.simpleName shouldBe "String"
    this.qualifiedName shouldBe "kotlin.String"
    this.properties shouldHaveSize 0
}


fun TypeInformation.shouldBeStringArray() {
    this.generics shouldHaveSize 1
    this.generics["T"]
        .also { it.shouldNotBeNull() }
        ?.also { genericType ->
            genericType.shouldBeString()
        }
    this.simpleName shouldBe "Array"
    this.qualifiedName shouldBe "kotlin.Array"
    this.properties shouldHaveSize 0
}

