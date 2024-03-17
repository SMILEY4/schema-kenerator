package io.github.smiley4.schemakenerator.reflection.models

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.Visibility

class TestSubClass(
    baseField: String,
    val additionalField: Int
) : TestOpenClass(baseField)


object TestSubClassMeta {

    val INLINE = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestSubClass"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestSubClass",
        simpleName = "TestSubClass",
        typeParameters = emptyMap(),
        supertypes = listOf(
            InlineTypeRef(TestOpenClassMeta.INLINE)
        ),
        members = listOf(
            PropertyData(
                name = "additionalField",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(MiscMeta.INT)
            ),
        )
    )

    val CONTEXT = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestSubClass"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestSubClass",
        simpleName = "TestSubClass",
        typeParameters = emptyMap(),
        supertypes = listOf(
            ContextTypeRef(TestOpenClassMeta.CONTEXT.id)
        ),
        members = listOf(
            PropertyData(
                name = "additionalField",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(MiscMeta.INT.id)
            ),
        )
    )

}