package io.github.smiley4.schemakenerator.kotlinxserialization.models

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.Visibility
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData

object MiscMeta {

    val UNIT = PrimitiveTypeData(
        id = TypeId("kotlin.Unit"),
        qualifiedName = "kotlin.Unit",
        simpleName = "Unit",
        typeParameters = emptyMap()
    )

    val ANY = WildcardTypeData()

    val WILDCARD = WildcardTypeData()


    val STRING = PrimitiveTypeData(
        id = TypeId("kotlin.String"),
        qualifiedName = "kotlin.String",
        simpleName = "String",
        typeParameters = emptyMap()
    )

    val INT = PrimitiveTypeData(
        id = TypeId("kotlin.Int"),
        qualifiedName = "kotlin.Int",
        simpleName = "Int",
        typeParameters = emptyMap()
    )

    val POLYMORPHIC_FUNCTION = ObjectTypeData(
        id = TypeId("kotlinx.serialization.Polymorphic<Function1>"),
        qualifiedName = "kotlinx.serialization.Polymorphic<Function1>",
        simpleName = "Polymorphic<Function1>", // note: information is lost here
        typeParameters = emptyMap()
    )

    fun listInline(item: BaseTypeData) = CollectionTypeData(
        id = TypeId("kotlin.collections.ArrayList<${item.id.id}>"),
        qualifiedName = "kotlin.collections.ArrayList",
        simpleName = "ArrayList",
        typeParameters = emptyMap(),
        itemType = PropertyData(
            name = "item",
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            type = InlineTypeRef(item)
        ),
    )

    fun listContext(item: BaseTypeData) = CollectionTypeData(
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

    fun arrayInline(item: BaseTypeData) = CollectionTypeData(
        id = TypeId("kotlin.Array<${item.id.id}>"),
        qualifiedName = "kotlin.Array",
        simpleName = "Array",
        typeParameters = emptyMap(),
        itemType = PropertyData(
            name = "item",
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            type = InlineTypeRef(item)
        ),
    )

    fun arrayContext(item: BaseTypeData) = CollectionTypeData(
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


    fun mapInline(key: BaseTypeData, value: BaseTypeData) = MapTypeData(
        id = TypeId("kotlin.collections.LinkedHashMap<${key.id.id},${value.id.id}>"),
        qualifiedName = "kotlin.collections.LinkedHashMap",
        simpleName = "LinkedHashMap",
        typeParameters = emptyMap(),
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


    fun mapContext(key: BaseTypeData, value: BaseTypeData) = MapTypeData(
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


}