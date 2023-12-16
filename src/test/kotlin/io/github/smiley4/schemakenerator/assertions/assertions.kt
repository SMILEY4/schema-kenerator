package io.github.smiley4.schemakenerator.assertions

import io.github.smiley4.schemakenerator.analysis.data.MemberData
import io.github.smiley4.schemakenerator.analysis.TypeContext
import io.github.smiley4.schemakenerator.analysis.data.TypeData
import io.github.smiley4.schemakenerator.analysis.data.TypeParameterData
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe


data class ExpectedTypeData(
    val simpleName: String? = null,
    val qualifiedName: String? = null,
    val typeParameters: Map<String, ExpectedTypeParameterData>? = null,
    val supertypeIds: List<String>? = null,
    val members: List<ExpectedMemberData>? = null,
)

data class ExpectedTypeParameterData(
    val name: String? = null,
    val typeId: String? = null,
    val nullable: Boolean? = null
)

data class ExpectedMemberData(
    val name: String? = null,
    val typeId: String? = null,
    val nullable: Boolean? = null
)

infix fun TypeData.shouldMatch(expected: ExpectedTypeData) {
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
    expected.supertypeIds?.also { this.supertypes.map { t -> t.id } shouldContainExactlyInAnyOrder it}
    expected.members?.also {
        this.members shouldHaveSize it.size
        this.members.map { m -> m.name } shouldContainExactlyInAnyOrder it.map { m -> m.name }
        it.forEach { em ->
            this.members.find { am -> am.name == em.name }!! shouldMatch em
        }
    }
}

infix fun TypeParameterData.shouldMatch(expected: ExpectedTypeParameterData) {
    expected.name?.also { this.name shouldBe it }
    expected.typeId?.also { this.type.id shouldBe it }
    expected.nullable?.also { this.nullable shouldBe it }
}

infix fun MemberData.shouldMatch(expected: ExpectedMemberData) {
    expected.name?.also { this.name shouldBe it }
    expected.typeId?.also { this.type.id shouldBe it }
    expected.nullable?.also { this.nullable shouldBe it }
}

infix fun TypeContext.shouldHave(expectedTypeIds: Collection<String>) {
    this.getIds() shouldContainExactlyInAnyOrder expectedTypeIds
}