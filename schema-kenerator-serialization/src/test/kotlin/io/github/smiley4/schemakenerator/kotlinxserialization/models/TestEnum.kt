package io.github.smiley4.schemakenerator.kotlinxserialization.models

import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeId
import kotlinx.serialization.Serializable

@Serializable
enum class TestEnum {
    RED, GREEN, BLUE
}

object TestEnumMeta {

    val INLINE = EnumTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.models.TestEnum"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.models.TestEnum",
        simpleName = "TestEnum",
        typeParameters = emptyMap(),
        enumConstants = listOf("RED", "GREEN", "BLUE")
    )

    val CONTEXT = EnumTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.kotlinxserialization.models.TestEnum"),
        qualifiedName = "io.github.smiley4.schemakenerator.kotlinxserialization.models.TestEnum",
        simpleName = "TestEnum",
        typeParameters = emptyMap(),
        enumConstants = listOf("RED", "GREEN", "BLUE")
    )

}