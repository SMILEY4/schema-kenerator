package io.github.smiley4.schemakenerator.reflection.models

import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeId

enum class TestEnum {
    RED, GREEN, BLUE
}

object TestEnumMeta {


    val CONTEXT = EnumTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestEnum"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestEnum",
        simpleName = "TestEnum",
        typeParameters = emptyMap(),
        enumConstants = listOf("RED", "GREEN", "BLUE")
    )

    val INLINE = EnumTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestEnum"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestEnum",
        simpleName = "TestEnum",
        typeParameters = emptyMap(),
        enumConstants = listOf("RED", "GREEN", "BLUE")
    )

}