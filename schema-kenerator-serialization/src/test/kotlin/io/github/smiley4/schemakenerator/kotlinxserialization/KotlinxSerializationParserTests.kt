package io.github.smiley4.schemakenerator.kotlinxserialization

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.kotlinxserialization.models.MiscMeta
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestAbstractClass
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestAbstractClassMeta
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassDeepGeneric
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassDeepGenericMeta
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassGeneric
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassGenericMeta
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassMixedTypes
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassMixedTypesMeta
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassSimple
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassSimpleMeta
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassWithEnumField
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassWithEnumFieldMeta
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassWithFunctionField
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestClassWithFunctionFieldMeta
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestEnumMeta
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestSubClass
import io.github.smiley4.schemakenerator.kotlinxserialization.models.TestSubClassMeta
import io.github.smiley4.schemakenerator.serialization.KotlinxSerializationTypeParser
import io.github.smiley4.schemakenerator.serialization.KotlinxSerializationTypeParserConfigBuilder
import io.github.smiley4.schemakenerator.serialization.getKType
import io.github.smiley4.schemakenerator.testutils.shouldMatch
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.reflect.KType

class KotlinxSerializationParserTests : FunSpec({

    context("kotlinx.serialization parser: basic (inline)") {
        withData(SIMPLE_DATA.filter { it.dataInline != null }) { data ->
            val context = TypeParserContext()
            val result = KotlinxSerializationTypeParser(context = context, config = data.configInline).parse(data.type)
            result.also { ref -> (ref is InlineTypeRef) shouldBe true }
            result.resolve(context)!!.also { type ->
                type.shouldMatch(data.dataInline!!)
            }
        }
    }

    context("kotlinx.serialization parser: basic (context)") {
        withData(SIMPLE_DATA.filter { it.dataParentContext != null }) { data ->
            val context = TypeParserContext()
            val result = KotlinxSerializationTypeParser(context = context, config = data.configContext).parse(data.type)
            result.also { ref -> (ref is ContextTypeRef) shouldBe true }
            result.resolve(context)!!.also { type ->
                type.shouldMatch(data.dataParentContext!!)
            }
            data.dataSupportingContext!!.forEach { typeData ->
                val actual = context.getData(typeData.id)
                actual.shouldNotBeNull()
                actual.shouldMatch(typeData)
            }
        }
    }

    context("kotlinx.serialization parser: result is always wildcard due to serializer being 'null' for type") {
        withData(listOf(
            getKType<TestClassGeneric<String>>()
        )) { type ->
            val contextInline = TypeParserContext()
            val resultInline = KotlinxSerializationTypeParser(context = contextInline, config = CONFIG_INLINE).parse(type)
            resultInline.also { ref -> (ref is InlineTypeRef) shouldBe true }
            resultInline.resolve(contextInline)!!.also { it.shouldMatch(MiscMeta.WILDCARD) }

            val contextContext = TypeParserContext()
            val resultContext = KotlinxSerializationTypeParser(context = contextContext, config = CONFIG_CONTEXT).parse(type)
            resultContext.also { ref -> (ref is ContextTypeRef) shouldBe true }
            resultContext.resolve(contextContext)!!.also { it.shouldMatch(MiscMeta.WILDCARD) }
        }
    }


    context("for debugging") {
        withData(listOf(
            getKType<TestClassDeepGeneric>()
        )) { type ->
            val context = TypeParserContext()
            val result = KotlinxSerializationTypeParser(context = context, config = CONFIG_CONTEXT).parse(type)
            println("$context $result")
        }
    }

}) {

    companion object {

        private class TestData(
            val type: KType,
            val dataInline: BaseTypeData?,
            val dataParentContext: BaseTypeData?,
            val dataSupportingContext: List<BaseTypeData>?,
            val configInline: KotlinxSerializationTypeParserConfigBuilder.() -> Unit = CONFIG_INLINE,
            val configContext: KotlinxSerializationTypeParserConfigBuilder.() -> Unit = CONFIG_CONTEXT,
            val testName: String = type.toString(),
        ) : WithDataTestName {
            override fun dataTestName() = testName
        }

        private val CONFIG_INLINE: (KotlinxSerializationTypeParserConfigBuilder.() -> Unit) = {
            inline = true
        }

        private val CONFIG_CONTEXT: (KotlinxSerializationTypeParserConfigBuilder.() -> Unit) = {
            inline = false
        }


        private val SIMPLE_DATA = listOf(
            TestData(
                type = getKType<Int>(),
                dataInline = MiscMeta.INT,
                dataParentContext = MiscMeta.INT,
                dataSupportingContext = emptyList()
            ),
            TestData(
                type = getKType<String>(),
                dataInline = MiscMeta.STRING,
                dataParentContext = MiscMeta.STRING,
                dataSupportingContext = emptyList()
            ),
            TestData(
                type = getKType<Any>(),
                dataInline = MiscMeta.ANY,
                dataParentContext = MiscMeta.ANY,
                dataSupportingContext = emptyList()
            ),
            TestData(
                type = getKType<Unit>(),
                dataInline = MiscMeta.UNIT,
                dataParentContext = MiscMeta.UNIT,
                dataSupportingContext = emptyList()
            ),
            TestData(
                type = getKType<TestClassSimple>(),
                dataInline = TestClassSimpleMeta.INLINE,
                dataParentContext = TestClassSimpleMeta.CONTEXT,
                dataSupportingContext = listOf(
                    MiscMeta.STRING
                )
            ),
            TestData(
                type = getKType<TestClassMixedTypes>(),
                dataInline = TestClassMixedTypesMeta.INLINE,
                dataParentContext = TestClassMixedTypesMeta.CONTEXT,
                dataSupportingContext = listOf(
                    MiscMeta.STRING,
                    TestClassSimpleMeta.CONTEXT,
                    MiscMeta.listContext(TestClassSimpleMeta.CONTEXT),
                    MiscMeta.mapContext(MiscMeta.STRING, TestClassSimpleMeta.CONTEXT),
                    MiscMeta.arrayContext(TestClassSimpleMeta.CONTEXT),
                )
            ),
            TestData(
                type = getKType<TestClassWithEnumField>(),
                dataInline = TestClassWithEnumFieldMeta.INLINE,
                dataParentContext = TestClassWithEnumFieldMeta.CONTEXT,
                dataSupportingContext = listOf(
                    TestEnumMeta.CONTEXT
                )
            ),
            TestData(
                type = getKType<TestClassWithFunctionField>(),
                dataInline = TestClassWithFunctionFieldMeta.INLINE,
                dataParentContext = TestClassWithFunctionFieldMeta.CONTEXT,
                dataSupportingContext = listOf(
                    MiscMeta.POLYMORPHIC_FUNCTION
                )
            ),
            TestData(
                type = getKType<TestClassDeepGeneric>(),
                dataInline = TestClassDeepGenericMeta.INLINE,
                dataParentContext = TestClassDeepGenericMeta.CONTEXT,
                dataSupportingContext = listOf(
                    MiscMeta.INT,
                    MiscMeta.STRING,
                    TestClassGenericMeta.context(MiscMeta.STRING)
                ),
            ),
            TestData(
                type = getKType<TestAbstractClass>(),
                dataInline = TestAbstractClassMeta.INLINE,
                dataParentContext = TestAbstractClassMeta.CONTEXT,
                dataSupportingContext = listOf(),
            ),
            TestData(
                type = getKType<TestSubClass>(),
                dataInline = TestSubClassMeta.INLINE,
                dataParentContext = TestSubClassMeta.CONTEXT,
                dataSupportingContext = listOf(
                    MiscMeta.INT,
                    MiscMeta.STRING,
                ),
            ),
        )

    }
}