package io.github.smiley4.schemakenerator.reflection.models

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.Visibility

open class TestOpenClass(
    val baseField: String
)

object TestOpenClassMeta {

    val INLINE = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestOpenClass"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestOpenClass",
        simpleName = "TestOpenClass",
        typeParameters = mutableMapOf(),
        members = mutableListOf(
            PropertyData(
                name = "baseField",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(MiscMeta.STRING)
            ),
        )
    )

    val CONTEXT = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestOpenClass"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestOpenClass",
        simpleName = "TestOpenClass",
        typeParameters = mutableMapOf(),
        members = mutableListOf(
            PropertyData(
                name = "baseField",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(MiscMeta.STRING.id)
            ),
        )
    )

}