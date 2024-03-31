package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.PropertyType
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.Visibility
import io.github.smiley4.schemakenerator.swagger.module.InliningGenerator
import io.kotest.core.spec.style.StringSpec

class InheritanceTest : StringSpec({

    "test deep inheritance structure" {

        val context = TypeDataContext()

        context.add(
            objectTypeData("BaseClass")
                .withMemberString("baseValue")
                .withSubType("ClassA")
                .withSubType("ClassB")
        )
        context.add(
            objectTypeData("ClassA")
                .withMemberString("a")
                .withSuperType("BaseClass")
                .withSubType("ClassA1")
                .withSubType("ClassA2")
        )
        context.add(
            objectTypeData("ClassA1")
                .withMemberString("a1")
                .withSuperType("ClassA")

        )
        context.add(
            objectTypeData("ClassA2")
                .withMemberString("a2")
                .withSuperType("ClassA")
        )
        context.add(
            objectTypeData("ClassB")
                .withMemberString("b")
                .withSuperType("BaseClass")
        )

        val schema = SwaggerSchemaGenerator()
            .withModule(InliningGenerator())
            .generate(ContextTypeRef(TypeId("BaseClass")), context)

     TODO("Assert")
    }

}) {

    companion object {

        private fun objectTypeData(name: String) = ObjectTypeData(
            id = TypeId(name),
            qualifiedName = name,
            simpleName = name,
            typeParameters = mutableMapOf(),
            members = mutableListOf()
        )

        private fun ObjectTypeData.withMemberString(name: String): ObjectTypeData {
            this.members.add(
                PropertyData(
                    name = name,
                    nullable = false,
                    visibility = Visibility.PUBLIC,
                    kind = PropertyType.PROPERTY,
                    type = InlineTypeRef(
                        PrimitiveTypeData(
                            id = TypeId("kotlin.String"),
                            simpleName = "String",
                            qualifiedName = "kotlin.String",
                            typeParameters = mutableMapOf()
                        )
                    )
                )
            )
            return this
        }

        private fun ObjectTypeData.withSubType(name: String): ObjectTypeData {
            this.subtypes.add(ContextTypeRef(TypeId(name)))
            return this
        }

        private fun ObjectTypeData.withSuperType(name: String): ObjectTypeData {
            this.supertypes.add(ContextTypeRef(TypeId(name)))
            return this
        }

    }

}