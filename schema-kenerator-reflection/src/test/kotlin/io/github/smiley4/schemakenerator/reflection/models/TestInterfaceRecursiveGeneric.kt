package io.github.smiley4.schemakenerator.reflection.models

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData

interface TestInterfaceRecursiveGeneric<T>


object TestInterfaceRecursiveGenericMeta {

    fun context(genericType: BaseTypeData) = ObjectTypeData(
        id = TypeId("io.github.smiley4.schemakenerator.reflection.models.TestInterfaceRecursiveGeneric<${genericType.id.id}>"),
        qualifiedName = "io.github.smiley4.schemakenerator.reflection.models.TestInterfaceRecursiveGeneric",
        simpleName = "TestInterfaceRecursiveGeneric",
        typeParameters = mutableMapOf(
            "T" to TypeParameterData(
                name = "T",
                type = ContextTypeRef(genericType.id),
                nullable = false
            ),
        ),
        members = mutableListOf()
    )

}