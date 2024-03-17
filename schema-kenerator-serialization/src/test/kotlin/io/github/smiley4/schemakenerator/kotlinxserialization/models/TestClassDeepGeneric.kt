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
data class TestClassDeepGeneric(
    val myInt: TestClassGeneric<Int>,
    val myString: TestClassGeneric<String>
)

object TestClassDeepGenericMeta {

    val INLINE = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassDeepGeneric"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassDeepGeneric",
        simpleName = "TestClassDeepGeneric",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "myInt",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(TestClassGenericMeta.inline(MiscMeta.INT))
            ),
            PropertyData(
                name = "myString",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(TestClassGenericMeta.inline(MiscMeta.STRING))
            ),
        )
    )

    val CONTEXT = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassDeepGeneric"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassDeepGeneric",
        simpleName = "TestClassDeepGeneric",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "myInt",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(TestClassGenericMeta.context(MiscMeta.INT).id)
            ),
            PropertyData(
                name = "myString",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(TestClassGenericMeta.context(MiscMeta.INT).id)
            ),
        )
    )


}