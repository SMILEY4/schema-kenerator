package io.github.smiley4.schemakenerator.reflection.models

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.Visibility

class TestClassMixedTypes(
    val myList: List<TestClassSimple>,
    val myMap: Map<String, TestClassSimple>,
    val myArray: Array<TestClassSimple>
)

object TestClassMixedTypesMeta {

    val CONTEXT = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassMixedTypes"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassMixedTypes",
        simpleName = "TestClassMixedTypes",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "myList",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(MiscMeta.listContext(TestClassSimpleMeta.CONTEXT).id)
            ),
            PropertyData(
                name = "myMap",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(MiscMeta.mapContext(MiscMeta.STRING, TestClassSimpleMeta.CONTEXT).id)
            ),
            PropertyData(
                name = "myArray",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(MiscMeta.arrayContext(TestClassSimpleMeta.CONTEXT).id)
            )
        )
    )

    val INLINE = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassMixedTypes"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassMixedTypes",
        simpleName = "TestClassMixedTypes",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "myArray",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(MiscMeta.arrayInline(TestClassSimpleMeta.INLINE))
            ),
            PropertyData(
                name = "myList",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(MiscMeta.listInline(TestClassSimpleMeta.INLINE))
            ),
            PropertyData(
                name = "myMap",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(MiscMeta.mapInline(MiscMeta.STRING, TestClassSimpleMeta.INLINE))
            ),
        )
    )

}