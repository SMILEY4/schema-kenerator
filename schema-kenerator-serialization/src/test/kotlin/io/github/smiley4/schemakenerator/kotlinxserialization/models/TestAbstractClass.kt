package io.github.smiley4.schemakenerator.kotlinxserialization.models

import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeId
import kotlinx.serialization.Serializable

@Serializable
abstract class TestAbstractClass {
    abstract val baseField: String
}

object TestAbstractClassMeta {

    val INLINE = ObjectTypeData(
        id = TypeId("kotlinx.serialization.Polymorphic<TestAbstractClass>"),
        qualifiedName = "kotlinx.serialization.Polymorphic<TestAbstractClass>",
        simpleName = "Polymorphic<TestAbstractClass>",
        typeParameters = emptyMap(),
        supertypes = emptyList(),
        subtypes = emptyList(),
        members = emptyList()
    )

    val CONTEXT = ObjectTypeData(
        id = TypeId("kotlinx.serialization.Polymorphic<TestAbstractClass>"),
        qualifiedName = "kotlinx.serialization.Polymorphic<TestAbstractClass>",
        simpleName = "Polymorphic<TestAbstractClass>",
        typeParameters = emptyMap(),
        supertypes = emptyList(),
        subtypes = emptyList(),
        members = emptyList()
    )

}