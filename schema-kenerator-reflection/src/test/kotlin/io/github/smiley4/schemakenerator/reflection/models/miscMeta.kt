package io.github.smiley4.schemakenerator.reflection.models

import io.github.smiley4.schemakenerator.core.parser.AnnotationData
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
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import io.github.smiley4.schemakenerator.core.parser.Visibility
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import java.time.LocalDateTime

object MiscMeta {

    val WILDCARD = WildcardTypeData()

    val ANY = PrimitiveTypeData(
        id = TypeId("kotlin.Any"),
        qualifiedName = "kotlin.Any",
        simpleName = "Any",
        typeParameters = mutableMapOf()
    )

    val UNIT = PrimitiveTypeData(
        id = TypeId("kotlin.Unit"),
        qualifiedName = "kotlin.Unit",
        simpleName = "Unit",
        typeParameters = mutableMapOf()
    )

    val STRING = PrimitiveTypeData(
        id = TypeId("kotlin.String"),
        qualifiedName = "kotlin.String",
        simpleName = "String",
        typeParameters = mutableMapOf()
    )

    val INT = PrimitiveTypeData(
        id = TypeId("kotlin.Int"),
        qualifiedName = "kotlin.Int",
        simpleName = "Int",
        typeParameters = mutableMapOf()
    )

    fun listContext(item: BaseTypeData) = CollectionTypeData(
        id = TypeId("kotlin.collections.List<${item.id.id}>"),
        qualifiedName = "kotlin.collections.List",
        simpleName = "List",
        typeParameters = mutableMapOf(
            "E" to TypeParameterData(
                name = "E",
                type = ContextTypeRef(item.id),
                nullable = false
            )
        ),
        supertypes = mutableListOf(),
        itemType = PropertyData(
            name = "item",
            nullable = false,
            visibility = Visibility.PUBLIC,
            kind = PropertyType.PROPERTY,
            type = ContextTypeRef(item.id),
            annotations = emptyList()
        ),
    )

    fun listInline(item: BaseTypeData) = CollectionTypeData(
        id = TypeId("kotlin.collections.List<${item.id.id}>"),
        qualifiedName = "kotlin.collections.List",
        simpleName = "List",
        typeParameters = mutableMapOf(
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

    fun arrayContext(item: BaseTypeData) = CollectionTypeData(
        id = TypeId("kotlin.Array<${item.id.id}>"),
        qualifiedName = "kotlin.Array",
        simpleName = "Array",
        typeParameters = mutableMapOf(
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

    fun arrayInline(item: BaseTypeData) = CollectionTypeData(
        id = TypeId("kotlin.Array<${item.id.id}>"),
        qualifiedName = "kotlin.Array",
        simpleName = "Array",
        typeParameters = mutableMapOf(
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


    fun mapContext(key: BaseTypeData, value: BaseTypeData) = MapTypeData(
        id = TypeId("kotlin.collections.Map<${key.id.id},${value.id.id}>"),
        qualifiedName = "kotlin.collections.Map",
        simpleName = "Map",
        typeParameters = mutableMapOf(
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

    fun mapInline(key: BaseTypeData, value: BaseTypeData)  = MapTypeData(
        id = TypeId("kotlin.collections.Map<${key.id.id},${value.id.id}>"),
        qualifiedName = "kotlin.collections.Map",
        simpleName = "Map",
        typeParameters = mutableMapOf(
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

    val LOCAL_DATE_TIME_CONTEXT = ObjectTypeData(
        id = TypeId("java.time.LocalDateTime"),
        qualifiedName = "java.time.LocalDateTime",
        simpleName = "LocalDateTime",
        supertypes = mutableListOf(
            ContextTypeRef(TypeId("java.time.temporal.Temporal")),
            ContextTypeRef(TypeId("java.time.temporal.TemporalAdjuster")),
            ContextTypeRef(TypeId("java.time.chrono.ChronoLocalDateTime<java.time.LocalDate>")),
        ),
        members = mutableListOf(
            PropertyData(
                name = "MIN",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(TypeId("java.time.LocalDateTime")),
                annotations = emptyList()
            ),
            PropertyData(
                name = "MAX",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(TypeId("java.time.LocalDateTime")),
                annotations = emptyList()
            )
        ),
        annotations = mutableListOf(
            AnnotationData("jdk.internal.ValueBased", NoOpAnnotation())
        )
    )

    val LOCAL_DATE_TIME_CUSTOMIZED_CONTEXT = PrimitiveTypeData(
        id = TypeId("java.time.LocalDateTime"),
        simpleName = LocalDateTime::class.simpleName!!,
        qualifiedName = LocalDateTime::class.qualifiedName!!,
        typeParameters = mutableMapOf()
    )

}