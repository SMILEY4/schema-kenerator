package io.github.smiley4.schemakenerator.reflection.models

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import io.github.smiley4.schemakenerator.core.parser.Visibility

data class TestClassGeneric<T>(
    val value: T
)

object TestClassGenericMeta {

    fun context(genericType: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassGeneric<${genericType.id.id}>"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassGeneric",
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

    fun inline(genericType: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassGeneric<${genericType.id.id}>"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassGeneric",
        simpleName = "TestClassGeneric",
        typeParameters = mapOf(
            "T" to TypeParameterData(
                name = "T",
                type = InlineTypeRef(genericType),
                nullable = false
            ),
        ),
        members = listOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(genericType)
            ),
        )
    )
}
