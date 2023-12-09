@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.smiley4.schemakenerator.models


class TestClassSimple(
    val someField: String
)

class TestClassSimpleDataTypes(

    val fieldByte: Byte,
    val fieldShort: Short,
    val fieldInt: Int,
    val fieldLong: Long,
    val fieldFloat: Float,
    val fieldDouble: Double,

    val fieldUByte: UByte,
    val fieldUShort: UShort,
    val fieldUInt: UInt,
    val fieldULong: ULong,

    val fieldUByteArray: UByteArray,
    val fieldByteArray: ByteArray,
    val fieldULongArray: ULongArray,
    val fieldLongArray: LongArray,

    val fieldBoolean: Boolean,
    val fieldBoolArray: BooleanArray,

    val fieldChar: Char,
    val fieldCharArray: CharArray,

    val fieldString: String,
    val fieldStringArray: Array<String>,

    val fieldObject: TestClassSimple,
    val fieldAny: Any,

    val fieldNullableString: String?,
    val fieldNullableObject: TestClassSimple?,
    val fieldNullableAny: Any?,

    val fieldStringList: List<String>,
    val fieldObjectList: List<TestClassSimple>,
    val fieldNullableObjectList: List<TestClassSimple?>,

    val fieldPrimitiveMap: Map<String, Int>,
    val fieldComplexMap: Map<TestClassSimple, Any?>,

)


data class TestClassGeneric<T>(
    val value: T
)

data class TestClassDeepGeneric<T>(
    val values: List<T>
)





