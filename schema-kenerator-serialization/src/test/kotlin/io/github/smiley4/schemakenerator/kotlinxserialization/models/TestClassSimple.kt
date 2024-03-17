package io.github.smiley4.schemakenerator.kotlinxserialization.models

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.Visibility
import kotlinx.serialization.Serializable

@Serializable
class TestClassSimple(
    val someField: String?
)

object TestClassSimpleMeta {

    val INLINE = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassSimple"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassSimple",
        simpleName = "TestClassSimple",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "someField",
                nullable = true,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(
                    PrimitiveTypeData(
                        id = TypeId("kotlin.String"),
                        simpleName = "String",
                        qualifiedName = "kotlin.String",
                        typeParameters = emptyMap()
                    )
                )
            )
        )
    )

    val CONTEXT = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassSimple"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassSimple",
        simpleName = "TestClassSimple",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "someField",
                nullable = true,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(TypeId("kotlin.String"))
            )
        )
    )

}