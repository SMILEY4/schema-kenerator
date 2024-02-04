package io.github.smiley4.schemakenerator.reflection

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
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import io.github.smiley4.schemakenerator.core.parser.Visibility

object TypeDataContext {

    fun wildcard() = PrimitiveTypeData(
        id = TypeId("*"),
        qualifiedName = "*",
        simpleName = "*",
        typeParameters = emptyMap()
    )

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
                type = ContextTypeRef(item.id),
                nullable = false
            )
        ),
        supertypes = emptyList(),
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
        typeParameters = mapOf(
            "T" to TypeParameterData(
                name = "T",
                type = ContextTypeRef(item.id),
                nullable = false
            )
        ),
        itemType = PropertyData(
            name = "item",
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            type = ContextTypeRef(item.id)
        ),
    )


    fun map(key: BaseTypeData, value: BaseTypeData) = MapTypeData(
        id = TypeId("kotlin.collections.Map<${key.id.id},${value.id.id}>"),
        qualifiedName = "kotlin.collections.Map",
        simpleName = "Map",
        typeParameters = mapOf(
            "K" to TypeParameterData(
                name = "K",
                type = ContextTypeRef(key.id),
                nullable = false
            ),
            "V" to TypeParameterData(
                name = "V",
                type = ContextTypeRef(value.id),
                nullable = false
            )
        ),
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
                type = ContextTypeRef(TypeId("kotlin.String"))
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
                type = ContextTypeRef(testEnum().id)
            ),
        )
    )

    fun testClassGeneric(genericType: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.TestClassGeneric<${genericType.id.id}>"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassGeneric",
        simpleName = "TestClassGeneric",
        typeParameters = mapOf(
            "T" to TypeParameterData(
                name = "T",
                type = ContextTypeRef(genericType.id),
                nullable = false
            ),
        ),
        members = listOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(genericType.id)
            ),
        )
    )

    fun testClassDeepGeneric(genericType: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.TestClassDeepGeneric<${genericType.id.id}>"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassDeepGeneric",
        simpleName = "TestClassDeepGeneric",
        typeParameters = mapOf(
            "E" to TypeParameterData(
                name = "E",
                type = ContextTypeRef(genericType.id),
                nullable = false
            ),
        ),
        members = listOf(
            PropertyData(
                name = "myValues",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(list(genericType).id)
            ),
        )
    )

    fun testSubClass() = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.TestSubClass"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestSubClass",
        simpleName = "TestSubClass",
        typeParameters = emptyMap(),
        supertypes = listOf(
            ContextTypeRef(testOpenClass().id)
        ),
        members = listOf(
            PropertyData(
                name = "additionalField",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(int().id)
            ),
        )
    )

    fun testOpenClass() = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.TestOpenClass"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestOpenClass",
        simpleName = "TestOpenClass",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "baseField",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(string().id)
            ),
        )
    )

    fun testClassRecursiveGeneric(genericType: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<${genericType.id.id}>"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric",
        simpleName = "TestClassRecursiveGeneric",
        typeParameters = mapOf(
            "T" to TypeParameterData(
                name = "T",
                type = ContextTypeRef(genericType.id),
                nullable = false
            ),
        ),
        supertypes = listOf(
            ContextTypeRef(TypeId("io.github.smiley4.schemakenerator.reflection.TestInterfaceRecursiveGeneric<io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<*>>"))
        ),
        members = listOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(genericType.id)
            ),
        )
    )

    fun testInterfaceRecursiveGeneric(genericType: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.TestInterfaceRecursiveGeneric<${genericType.id.id}>"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.TestInterfaceRecursiveGeneric",
        simpleName = "TestInterfaceRecursiveGeneric",
        typeParameters = mapOf(
            "T" to TypeParameterData(
                name = "T",
                type = ContextTypeRef(genericType.id),
                nullable = false
            ),
        ),
        members = emptyList()
    )

}
