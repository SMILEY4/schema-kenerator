package io.github.smiley4.schemakenerator.kotlinxserialization.models

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.Visibility
import kotlinx.serialization.Serializable

@Serializable
data class TestClassGeneric<T>(
    val genericValue: T
)

object TestClassGenericMeta {

    fun inline(type: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassGeneric"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassGeneric",
        simpleName = "TestClassGeneric",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "genericValue",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(type)
            ),
        )
    )

    fun context(type: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassGeneric"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassGeneric",
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

}