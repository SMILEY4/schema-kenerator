package io.github.smiley4.schemakenerator.reflection.models

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.Visibility
import java.time.LocalDateTime

class TestClassLocalDateTime(
    val timestamp: LocalDateTime
)

object TestClassLocalDateTimeMeta {

    val INLINE = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassLocalDateTime"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassLocalDateTime",
        simpleName = "TestClassLocalDateTime",
        typeParameters = emptyMap(),
        members = listOf(
            PropertyData(
                name = "timestamp",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = InlineTypeRef(
                    PrimitiveTypeData(
                        id = TypeId("java.time.LocalDateTime"),
                        simpleName = LocalDateTime::class.simpleName!!,
                        qualifiedName = LocalDateTime::class.qualifiedName!!,
                        typeParameters = emptyMap()
                    )
                )
            ),
        )
    )

    val CONTEXT = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassLocalDateTime"),
        simpleName = "TestClassLocalDateTime",
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassLocalDateTime",
        members = listOf(
            PropertyData(
                name = "timestamp",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(TypeId("java.time.LocalDateTime")),
                annotations = emptyList()
            )
        )
    )

}