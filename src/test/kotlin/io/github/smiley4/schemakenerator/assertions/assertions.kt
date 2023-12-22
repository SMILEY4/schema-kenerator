package io.github.smiley4.schemakenerator.assertions

import io.github.smiley4.schemakenerator.parser.core.BaseTypeData
import io.github.smiley4.schemakenerator.parser.core.EnumTypeData
import io.github.smiley4.schemakenerator.parser.core.ObjectTypeData
import io.github.smiley4.schemakenerator.parser.core.PrimitiveTypeData
import io.github.smiley4.schemakenerator.parser.core.PropertyData
import io.github.smiley4.schemakenerator.parser.core.TypeParameterData
import io.github.smiley4.schemakenerator.parser.core.TypeParserContext
import io.github.smiley4.schemakenerator.parser.core.WildcardTypeData
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe


open class ExpectedPrimitiveTypeData(
    val simpleName: String? = null,
    val qualifiedName: String? = null,
    val typeParameters: Map<String, ExpectedTypeParameterData>? = null,
)

open class ExpectedObjectTypeData(
    simpleName: String? = null,
    qualifiedName: String? = null,
    typeParameters: Map<String, ExpectedTypeParameterData>? = null,
    val subtypeIds: List<String>? = null,
    val supertypeIds: List<String>? = null,
    val members: List<ExpectedPropertyData>? = null
) : ExpectedPrimitiveTypeData(simpleName, qualifiedName, typeParameters)

class ExpectedEnumTypeData(
    simpleName: String? = null,
    qualifiedName: String? = null,
    typeParameters: Map<String, ExpectedTypeParameterData>? = null,
    subtypeIds: List<String>? = null,
    supertypeIds: List<String>? = null,
    members: List<ExpectedPropertyData>? = null,
    val enumConstants: List<String>? = null
) : ExpectedObjectTypeData(simpleName, qualifiedName, typeParameters, subtypeIds, supertypeIds, members)

data class ExpectedTypeParameterData(
    val name: String? = null,
    val typeId: String? = null,
    val nullable: Boolean? = null
)

data class ExpectedPropertyData(
    val name: String? = null,
    val typeId: String? = null,
    val nullable: Boolean? = null
)

infix fun BaseTypeData.shouldMatch(expected: ExpectedPrimitiveTypeData) {
    if (this is PrimitiveTypeData) {
        expected.simpleName?.also { this.simpleName shouldBe it }
        expected.qualifiedName?.also { this.qualifiedName shouldBe it }
        expected.typeParameters?.also {
            this.typeParameters.keys shouldContainExactlyInAnyOrder it.keys
            this.typeParameters.keys.forEach { key ->
                val a = this.typeParameters[key]!!
                val b = it[key]!!
                a.shouldMatch(b)
            }
        }
    } else {
        throw Exception("this type is not a ${PrimitiveTypeData::simpleName}")
    }
}

infix fun BaseTypeData.shouldMatch(expected: ExpectedObjectTypeData) {
    if (this is ObjectTypeData) {
        expected.simpleName?.also { this.simpleName shouldBe it }
        expected.qualifiedName?.also { this.qualifiedName shouldBe it }
        expected.typeParameters?.also {
            this.typeParameters.keys shouldContainExactlyInAnyOrder it.keys
            this.typeParameters.keys.forEach { key ->
                val a = this.typeParameters[key]!!
                val b = it[key]!!
                a.shouldMatch(b)
            }
        }
        expected.subtypeIds?.also { this.subtypes.map { t -> t.id } shouldContainExactlyInAnyOrder it }
        expected.supertypeIds?.also { this.supertypes.map { t -> t.id } shouldContainExactlyInAnyOrder it }
        expected.members?.also {
            this.members shouldHaveSize it.size
            this.members.map { m -> m.name } shouldContainExactlyInAnyOrder it.map { m -> m.name }
            it.forEach { em ->
                this.members.find { am -> am.name == em.name }!! shouldMatch em
            }
        }
    } else {
        throw Exception("this type is not a ${ObjectTypeData::simpleName}")
    }
}

infix fun BaseTypeData.shouldMatch(expected: ExpectedEnumTypeData) {
    if (this is EnumTypeData) {
        this.shouldMatch(expected as ExpectedObjectTypeData)
        expected.enumConstants?.also { this.enumConstants.shouldContainExactlyInAnyOrder(it) }
    } else {
        throw Exception("this type is not a ${ObjectTypeData::simpleName}")
    }
}


fun BaseTypeData.shouldMatchWildcard() {
    if (this !is WildcardTypeData) {
        throw Exception("this type is not a ${ObjectTypeData::simpleName}")
    }
}

infix fun TypeParameterData.shouldMatch(expected: ExpectedTypeParameterData) {
    expected.name?.also { this.name shouldBe it }
    expected.typeId?.also { this.type.id shouldBe it }
    expected.nullable?.also { this.nullable shouldBe it }
}

infix fun PropertyData.shouldMatch(expected: ExpectedPropertyData) {
    expected.name?.also { this.name shouldBe it }
    expected.typeId?.also { this.type.id shouldBe it }
    expected.nullable?.also { this.nullable shouldBe it }
}

infix fun TypeParserContext.shouldHaveExactly(expectedTypeIds: Collection<String>) {
    this.getIds() shouldContainExactlyInAnyOrder expectedTypeIds
}