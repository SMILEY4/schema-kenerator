package io.github.smiley4.schemakenerator.kotlinxserialization

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.Visibility

object TypeDataContext {

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
        id = TypeId("kotlin.collections.ArrayList<${item.id.id}>"),
        qualifiedName = "kotlin.collections.ArrayList",
        simpleName = "ArrayList",
        typeParameters = emptyMap(),
        itemType = PropertyData(
            name = "item",
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            type = ContextTypeRef(item.id)
        ),
    )

    fun array(item: BaseTypeData) = CollectionTypeData(
        id = TypeId("kotlin.Array<${item.id.id}>"),
        qualifiedName = "kotlin.Array",
        simpleName = "Array",
        typeParameters = emptyMap(),
        itemType = PropertyData(
            name = "item",
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            type = ContextTypeRef(item.id)
        ),
    )


    fun map(key: BaseTypeData, value: BaseTypeData) = MapTypeData(
        id = TypeId("kotlin.collections.LinkedHashMap<${key.id.id},${value.id.id}>"),
        qualifiedName = "kotlin.collections.LinkedHashMap",
        simpleName = "LinkedHashMap",
        typeParameters = emptyMap(),
        keyType = PropertyData(
            name = "key",
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            type = ContextTypeRef(key.id)
        ),
        valueType = PropertyData(
            name = "value",
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            type = ContextTypeRef(value.id)
        ),
    )


    fun testClassSimple() = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassSimple",
        simpleName = "TestClassSimple",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "someField",
                nullable = true,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(TypeId("kotlin.String"))
            )
        )
    )


    fun testClassMixedTypes() = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestClassMixedTypes"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassMixedTypes",
        simpleName = "TestClassMixedTypes",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "myList",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(list(testClassSimple()).id)
            ),
            PropertyData(
                name = "myMap",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(map(string(), testClassSimple()).id)
            ),
            PropertyData(
                name = "myArray",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(array(testClassSimple()).id)
            )
        )
    )


    fun testEnum() = EnumTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestEnum"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.TestEnum",
        simpleName = "TestEnum",
        typeParameters = emptyMap(),
        enumConstants = listOf("RED", "GREEN", "BLUE")
    )

    fun testClassWithEnumField() = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithEnumField"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithEnumField",
        simpleName = "TestClassWithEnumField",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(testEnum().id)
            ),
        )
    )

    fun polymorphicFunction() = ObjectTypeData(
        id = TypeId("kotlinx.serialization.Polymorphic<Function1>"),
        qualifiedName = "kotlinx.serialization.Polymorphic<Function1>",
        simpleName = "Polymorphic<Function1>", // note: information is lost here
        typeParameters = emptyMap()
    )

    fun testClassWithFunctionField() = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithFunctionField"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassWithFunctionField",
        simpleName = "TestClassWithFunctionField",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(polymorphicFunction().id)
            ),
        )
    )

    fun testClassGeneric(type: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestClassGeneric"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassGeneric",
        simpleName = "TestClassGeneric",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "genericValue",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(type.id)
            ),
        )
    )

    fun testClassDeepGeneric() = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.TestClassDeepGeneric"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.TestClassDeepGeneric",
        simpleName = "TestClassDeepGeneric",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "myInt",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(testClassGeneric(int()).id)
            ),
            PropertyData(
                name = "myString",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(testClassGeneric(string()).id)
            ),
        )
    )

}
