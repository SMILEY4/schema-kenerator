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
class TestClassWithFunctionField(
    val value: (x: Int) -> String
)

object TestClassWithFunctionFieldMeta {

    val INLINE = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassWithFunctionField"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassWithFunctionField",
        simpleName = "TestClassWithFunctionField",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(MiscMeta.POLYMORPHIC_FUNCTION)
            ),
        )
    )

    val CONTEXT = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassWithFunctionField"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassWithFunctionField",
        simpleName = "TestClassWithFunctionField",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(MiscMeta.POLYMORPHIC_FUNCTION.id)
            ),
        )
    )

}