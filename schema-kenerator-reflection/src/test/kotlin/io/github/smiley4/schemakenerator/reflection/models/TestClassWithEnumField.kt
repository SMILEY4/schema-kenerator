package io.github.smiley4.schemakenerator.reflection.models

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.Visibility

class TestClassWithEnumField(
    val value: TestEnum
)

object TestClassWithEnumFieldMeta {

    val INLINE = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassWithEnumField"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassWithEnumField",
        simpleName = "TestClassWithEnumField",
        typeParameters = mutableMapOf(),
        members = mutableListOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(TestEnumMeta.INLINE)
            ),
        )
    )

    val CONTEXT = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassWithEnumField"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassWithEnumField",
        simpleName = "TestClassWithEnumField",
        typeParameters = mutableMapOf(),
        members = mutableListOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(TestEnumMeta.CONTEXT.id)
            ),
        )
    )
}