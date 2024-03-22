package io.github.smiley4.schemakenerator.reflection.models

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import io.github.smiley4.schemakenerator.core.parser.Visibility

data class TestClassRecursiveGeneric<T>(
    val value: T
) : TestInterfaceRecursiveGeneric<TestClassRecursiveGeneric<*>>


object TestClassRecursiveGenericMeta {

    fun context(genericType: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestClassRecursiveGeneric<${genericType.id.id}>"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestClassRecursiveGeneric",
        simpleName = "TestClassRecursiveGeneric",
        typeParameters = mutableMapOf(
            "T" to TypeParameterData(
                name = "T",
                type = ContextTypeRef(genericType.id),
                nullable = false
            ),
        ),
        supertypes = mutableListOf(
            ContextTypeRef(TypeId("io.github.smiley4.schemakenerator.reflection.models.TestInterfaceRecursiveGeneric<io.github.smiley4.schemakenerator.reflection.models.TestClassRecursiveGeneric<*>>"))
        ),
        members = mutableListOf(
            PropertyData(
                name = "value",
                nullable = false,
                visibility = Visibility.PUBLIC,
                kind = PropertyType.PROPERTY,
                type = ContextTypeRef(genericType.id)
            ),
        )
    )

}