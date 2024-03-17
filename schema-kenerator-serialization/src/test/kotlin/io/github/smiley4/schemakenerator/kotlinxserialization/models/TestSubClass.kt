package io.github.smiley4.schemakenerator.kotlinxserialization.models

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.Visibility
import kotlinx.serialization.Serializable

@Serializable
class TestSubClass(
    override val baseField: String,
    val additionalField: Int
) : TestAbstractClass()


object TestSubClassMeta {

    val INLINE = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.models.TestSubClass"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.models.TestSubClass",
        simpleName = "TestSubClass",
        typeParameters = emptyMap(),
        supertypes = emptyList(),
        subtypes = emptyList(),
        members = listOf(
            PropertyData(
                name = "baseField",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(MiscMeta.STRING)
            ),
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
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.models.TestSubClass"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.models.TestSubClass",
        simpleName = "TestSubClass",
        typeParameters = emptyMap(),
        supertypes = emptyList(),
        subtypes = emptyList(),
        members = listOf(
            PropertyData(
                name = "baseField",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(MiscMeta.STRING.id)
            ),
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