package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import io.github.smiley4.schemakenerator.core.parser.Visibility
import java.time.LocalDateTime

object TypeDataInline {

    fun any() = PrimitiveTypeData(
        id = TypeId("kotlin.Any"),
        qualifiedName = "kotlin.Any",
        simpleName = "Any",
        typeParameters = emptyMap()
    )

    fun unit() = PrimitiveTypeData(
        id = TypeId("kotlin.Unit"),
        qualifiedName = "kotlin.Unit",
        simpleName = "Unit",
        typeParameters = emptyMap()
    )

    fun string() = PrimitiveTypeData(
        id = TypeId("kotlin.String"),
        qualifiedName = "kotlin.String",
        simpleName = "String",
        typeParameters = emptyMap()
    )

    fun int() = PrimitiveTypeData(
        id = TypeId("kotlin.Int"),
        qualifiedName = "kotlin.Int",
        simpleName = "Int",
        typeParameters = emptyMap()
    )

    fun list(item: BaseTypeData) = CollectionTypeData(
        id = TypeId("kotlin.collections.List<${item.id.id}>"),
        qualifiedName = "kotlin.collections.List",
        simpleName = "List",
        typeParameters = mapOf(
            "E" to TypeParameterData(
                name = "E",
                nullable = false,
                type = InlineTypeRef(item)
            )
        ),
        itemType = PropertyData(
            name = "item",
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            type = InlineTypeRef(item)
        ),
    )

    fun array(item: BaseTypeData) = CollectionTypeData(
        id = TypeId("kotlin.Array<${item.id.id}>"),
        qualifiedName = "kotlin.Array",
        simpleName = "Array",
        typeParameters = mapOf(
            "T" to TypeParameterData(
                name = "T",
                nullable = false,
                type = InlineTypeRef(item)
            )
        ),
        itemType = PropertyData(
            name = "item",
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            type = InlineTypeRef(item)
        ),
    )


    fun map(key: BaseTypeData, value: BaseTypeData)  = MapTypeData(
        id = TypeId("kotlin.collections.Map<${key.id.id},${value.id.id}>"),
        qualifiedName = "kotlin.collections.Map",
        simpleName = "Map",
        typeParameters = mapOf(
            "K" to TypeParameterData(
                name = "K",
                nullable = false,
                type = InlineTypeRef(key)
            ),
            "V" to TypeParameterData(
                name = "V",
                nullable = false,
                type = InlineTypeRef(value)
            )
        ),
        keyType = PropertyData(
            name = "key",
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            type = InlineTypeRef(key)
        ),
        valueType = PropertyData(
            name = "value",
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            type = InlineTypeRef(value)
        ),
    )



    fun testClassSimple() = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.TestClassSimple"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassSimple",
        simpleName = "TestClassSimple",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "someField",
                nullable = true,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(
                    PrimitiveTypeData(
                        id = TypeId("kotlin.String"),
                        simpleName = "String",
                        qualifiedName = "kotlin.String",
                        typeParameters = emptyMap()
                    )
                )
            )
        )
    )


    fun testClassMixedTypes() = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.TestClassMixedTypes"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassMixedTypes",
        simpleName = "TestClassMixedTypes",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "myArray",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(array(testClassSimple()))
            ),
            PropertyData(
                name = "myList",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(list(testClassSimple()))
            ),
            PropertyData(
                name = "myMap",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(map(string(), testClassSimple()))
            ),
        )
    )

    fun testEnum() = EnumTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.TestEnum"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestEnum",
        simpleName = "TestEnum",
        typeParameters = emptyMap(),
        enumConstants = listOf("RED", "GREEN", "BLUE")
    )

    fun testClassWithEnumField() = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.TestClassWithEnumField"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassWithEnumField",
        simpleName = "TestClassWithEnumField",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(testEnum())
            ),
        )
    )

    fun testClassLocalDateTimeCustomized() = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassLocalDateTime",
        simpleName = "TestClassLocalDateTime",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "timestamp",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(
                    PrimitiveTypeData(
                        id = TypeId("java.time.LocalDateTime"),
                        simpleName = LocalDateTime::class.simpleName!!,
                        qualifiedName = LocalDateTime::class.qualifiedName!!,
                        typeParameters = emptyMap()
                    )
                )
            ),
        )
    )

}
