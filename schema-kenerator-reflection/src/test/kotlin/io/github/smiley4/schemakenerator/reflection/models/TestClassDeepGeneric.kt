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

data class TestClassDeepGeneric<E>(
    val myValues: List<E>
)

object TestClassDeepGenericMeta {

    fun context(genericType: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassDeepGeneric<${genericType.id.id}>"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassDeepGeneric",
        simpleName = "TestClassDeepGeneric",
        typeParameters = mutableMapOf(
            "E" to TypeParameterData(
                name = "E",
                type = ContextTypeRef(genericType.id),
                nullable = false
            ),
        ),
        members = mutableListOf(
            PropertyData(
                name = "myValues",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(MiscMeta.listContext(genericType).id)
            ),
        )
    )

    fun inline(genericType: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassDeepGeneric<${genericType.id.id}>"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassDeepGeneric",
        simpleName = "TestClassDeepGeneric",
        typeParameters = mutableMapOf(
            "E" to TypeParameterData(
                name = "E",
                type = InlineTypeRef(genericType),
                nullable = false
            ),
        ),
        members = mutableListOf(
            PropertyData(
                name = "myValues",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(MiscMeta.listInline(genericType))
            ),
        )
    )

}
