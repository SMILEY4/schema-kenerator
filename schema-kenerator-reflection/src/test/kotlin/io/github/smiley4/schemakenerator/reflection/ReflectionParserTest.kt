package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.reflection.models.MiscMeta
import io.github.smiley4.schemakenerator.reflection.models.TestClassDeepGeneric
import io.github.smiley4.schemakenerator.reflection.models.TestClassDeepGenericMeta
import io.github.smiley4.schemakenerator.reflection.models.TestClassGeneric
import io.github.smiley4.schemakenerator.reflection.models.TestClassGenericMeta
import io.github.smiley4.schemakenerator.reflection.models.TestClassLocalDateTime
import io.github.smiley4.schemakenerator.reflection.models.TestClassLocalDateTimeMeta
import io.github.smiley4.schemakenerator.reflection.models.TestClassMixedTypes
import io.github.smiley4.schemakenerator.reflection.models.TestClassMixedTypesMeta
import io.github.smiley4.schemakenerator.reflection.models.TestClassRecursiveGeneric
import io.github.smiley4.schemakenerator.reflection.models.TestClassRecursiveGenericMeta
import io.github.smiley4.schemakenerator.reflection.models.TestClassSimple
import io.github.smiley4.schemakenerator.reflection.models.TestClassSimpleMeta
import io.github.smiley4.schemakenerator.reflection.models.TestClassWithAnnotations
import io.github.smiley4.schemakenerator.reflection.models.TestClassWithAnnotationsMeta
import io.github.smiley4.schemakenerator.reflection.models.TestClassWithEnumField
import io.github.smiley4.schemakenerator.reflection.models.TestClassWithEnumFieldMeta
import io.github.smiley4.schemakenerator.reflection.models.TestClassWithMethods
import io.github.smiley4.schemakenerator.reflection.models.TestEnumMeta
import io.github.smiley4.schemakenerator.reflection.models.TestInterfaceRecursiveGenericMeta
import io.github.smiley4.schemakenerator.reflection.models.TestOpenClassMeta
import io.github.smiley4.schemakenerator.reflection.models.TestSubClass
import io.github.smiley4.schemakenerator.reflection.models.TestSubClassMeta
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
import io.github.smiley4.schemakenerator.testutils.shouldMatch
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import kotlin.reflect.KType

class ReflectionParserTest : FunSpec({

    context("reflection parser: basic (inline)") {
        withData(SIMPLE_DATA.filter { it.dataInline != null }) { data ->
            val context = TypeDataContext()
            val result = ReflectionTypeParser(context = context, config = data.configInline).parse(data.type)
            result.also { ref -> (ref is InlineTypeRef) shouldBe true }
            result.resolve(context)!!.also { type ->
                type.shouldMatch(data.dataInline!!)
            }
        }
    }

    context("reflection parser: basic (context)") {
        withData(SIMPLE_DATA.filter { it.dataParentContext != null }) { data ->
            val context = TypeDataContext()
            val result = ReflectionTypeParser(context = context, config = data.configContext).parse(data.type)
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

    context("reflection parser: filters (context)") {
        withData(FILTER_DATA) { data ->
            val context = TypeDataContext()
            ReflectionTypeParser(
                context = context,
                config = data.configContext
            ).parse(data.type)
                .let { it.resolve(context)!! }
                .also { type ->
                    val members = (type as ObjectTypeData).members.map { it.name }
                    members shouldContainExactlyInAnyOrder data.members
                }
        }
    }

    context("reflection parser: filters (inline)") {
        withData(FILTER_DATA) { data ->
            val context = TypeDataContext()
            ReflectionTypeParser(
                context = context,
                config = data.configInline
            ).parse(data.type)
                .let { it.resolve(context)!! }
                .also { type ->
                    val members = (type as ObjectTypeData).members.map { it.name }
                    members shouldContainExactlyInAnyOrder data.members
                }
        }
    }

    context("reflection parser: StackOverflowError (infinite loops) when inlining") {
        withData(listOf(
            getKType<TestClassLocalDateTime>(),
            getKType<TestClassRecursiveGeneric<String>>()
        )) { type ->
            shouldThrow<StackOverflowError> {
                val context = TypeDataContext()
                ReflectionTypeParser(context = context, config = CONFIG_INLINE).parse(type)
            }
        }
    }

    context("for debugging") {
        withData(listOf(
            getKType<TestSubClass>()
        )) { type ->
            val context = TypeDataContext()
            val result = ReflectionTypeParser(context = context, config = CONFIG_INLINE).parse(type)
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
            val configInline: ReflectionTypeParserConfigBuilder.() -> Unit = CONFIG_INLINE,
            val configContext: ReflectionTypeParserConfigBuilder.() -> Unit = CONFIG_CONTEXT,
            val testName: String = type.toString(),
        ) : WithDataTestName {
            override fun dataTestName() = testName
        }

        private val CONFIG_INLINE: (ReflectionTypeParserConfigBuilder.() -> Unit) = {
            inline = true
        }

        private val CONFIG_CONTEXT: (ReflectionTypeParserConfigBuilder.() -> Unit) = {
            inline = false
        }

        private val CONFIG_INLINE_CUSTOM_LOCAL_DATE_TIME: (ReflectionTypeParserConfigBuilder.() -> Unit) = {
            inline = true
            registerParser(LocalDateTime::class) { id, _ ->
                PrimitiveTypeData(
                    id = id,
                    simpleName = LocalDateTime::class.simpleName!!,
                    qualifiedName = LocalDateTime::class.qualifiedName!!,
                    typeParameters = mutableMapOf()
                )
            }
        }

        private val CONFIG_CONTEXT_CUSTOM_LOCAL_DATE_TIME: (ReflectionTypeParserConfigBuilder.() -> Unit) = {
            inline = false
            registerParser(LocalDateTime::class) { id, _ ->
                PrimitiveTypeData(
                    id = id,
                    simpleName = LocalDateTime::class.simpleName!!,
                    qualifiedName = LocalDateTime::class.qualifiedName!!,
                    typeParameters = mutableMapOf()
                )
            }
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
                type = getKType<TestClassLocalDateTime>(),
                dataInline = null, // results in exception (infinite recursion) -> see other test case
                dataParentContext = TestClassLocalDateTimeMeta.CONTEXT,
                dataSupportingContext = listOf(
                    MiscMeta.LOCAL_DATE_TIME_CONTEXT
                    // ... + some other java types
                )
            ),
            TestData(
                testName = getKType<TestClassLocalDateTime>().toString() + " (customized)",
                type = getKType<TestClassLocalDateTime>(),
                dataInline = TestClassLocalDateTimeMeta.INLINE,
                dataParentContext = TestClassLocalDateTimeMeta.CONTEXT,
                dataSupportingContext = listOf(
                    MiscMeta.LOCAL_DATE_TIME_CUSTOMIZED_CONTEXT
                ),
                configContext = CONFIG_CONTEXT_CUSTOM_LOCAL_DATE_TIME,
                configInline = CONFIG_INLINE_CUSTOM_LOCAL_DATE_TIME,
            ),
            TestData(
                type = getKType<TestClassGeneric<String>>(),
                dataInline = TestClassGenericMeta.inline(MiscMeta.STRING),
                dataParentContext = TestClassGenericMeta.context(MiscMeta.STRING),
                dataSupportingContext = listOf(
                    MiscMeta.STRING
                ),
            ),
            TestData(
                type = getKType<TestClassGeneric<TestClassGeneric<String>>>(),
                dataInline = TestClassGenericMeta.inline(TestClassGenericMeta.inline(MiscMeta.STRING)),
                dataParentContext = TestClassGenericMeta.context(TestClassGenericMeta.context(MiscMeta.STRING)),
                dataSupportingContext = listOf(
                    TestClassGenericMeta.context(MiscMeta.STRING),
                    MiscMeta.STRING,
                ),
            ),
            TestData(
                type = getKType<TestClassDeepGeneric<String>>(),
                dataInline = TestClassDeepGenericMeta.inline(MiscMeta.STRING),
                dataParentContext = TestClassDeepGenericMeta.context(MiscMeta.STRING),
                dataSupportingContext = listOf(
                    MiscMeta.STRING,
                    MiscMeta.listContext(MiscMeta.STRING)
                ),
            ),
            TestData(
                type = getKType<TestClassGeneric<*>>(),
                dataInline = TestClassGenericMeta.inline(MiscMeta.WILDCARD),
                dataParentContext = TestClassGenericMeta.context(MiscMeta.WILDCARD),
                dataSupportingContext = listOf(
                    MiscMeta.WILDCARD
                ),
            ),
            TestData(
                type = getKType<TestClassRecursiveGeneric<String>>(),
                dataInline = null, // results in exception (infinite recursion) -> see other test case
                dataParentContext = TestClassRecursiveGenericMeta.context(MiscMeta.STRING),
                dataSupportingContext = listOf(
                    MiscMeta.WILDCARD,
                    MiscMeta.STRING,
                    TestClassRecursiveGenericMeta.context(MiscMeta.WILDCARD),
                    TestInterfaceRecursiveGenericMeta.context(TestClassRecursiveGenericMeta.context(MiscMeta.WILDCARD))
                ),
            ),
            TestData(
                type = getKType<TestSubClass>(),
                dataInline = TestSubClassMeta.INLINE,
                dataParentContext = TestSubClassMeta.CONTEXT,
                dataSupportingContext = listOf(
                    TestOpenClassMeta.CONTEXT,
                    MiscMeta.STRING,
                    MiscMeta.INT
                ),
            ),
            TestData(
                type = getKType<TestClassWithAnnotations>(),
                dataInline = TestClassWithAnnotationsMeta.INLINE,
                dataParentContext = TestClassWithAnnotationsMeta.CONTEXT,
                dataSupportingContext = listOf(
                    MiscMeta.STRING
                ),
            ),
        )

        data class FilterTestData(
            val type: KType,
            val members: List<String>,
            val configInline: ReflectionTypeParserConfigBuilder.() -> Unit = CONFIG_INLINE,
            val configContext: ReflectionTypeParserConfigBuilder.() -> Unit = CONFIG_CONTEXT,
            val testName: String = type.toString(),
        ) : WithDataTestName {
            override fun dataTestName() = testName
        }

        val FILTER_DATA = listOf(
            FilterTestData(
                testName = "default config",
                configInline = {},
                configContext = {},
                type = getKType<TestClassWithMethods>(),
                members = listOf(
                    "someText",
                    "myFlag",
                    "isEnabled",
                    // "hiddenField",
                    // "calculateValue",
                    // "compare",
                    // "isDisabled",
                    // "hiddenFunction",
                    // "equals",
                    // "hashCode",
                    // "toString",
                )
            ),
            FilterTestData(
                testName = "include getters",
                configInline = { includeGetters = true },
                configContext = { includeGetters = true },
                type = getKType<TestClassWithMethods>(),
                members = listOf(
                    "someText",
                    "myFlag",
                    "isEnabled",
                    // "hiddenField",
                    // "calculateValue",
                    // "compare",
                    "isDisabled",
                    // "hiddenFunction",
                    // "equals",
                    // "hashCode",
                    // "toString",
                )
            ),
            FilterTestData(
                testName = "include weak getters",
                configInline = { includeWeakGetters = true },
                configContext = { includeWeakGetters = true },
                type = getKType<TestClassWithMethods>(),
                members = listOf(
                    "someText",
                    "myFlag",
                    "isEnabled",
                    // "hiddenField",
                    "calculateValue",
                    // "compare",
                    // "isDisabled",
                    // "hiddenFunction",
                    // "equals",
                    "hashCode",
                    "toString",
                )
            ),
            FilterTestData(
                testName = "include all getters",
                configInline = {
                    includeGetters = true
                    includeWeakGetters = true
                },
                configContext = {
                    includeGetters = true
                    includeWeakGetters = true
                },
                type = getKType<TestClassWithMethods>(),
                members = listOf(
                    "someText",
                    "myFlag",
                    "isEnabled",
                    // "hiddenField",
                    "calculateValue",
                    // "compare",
                    "isDisabled",
                    // "hiddenFunction",
                    // "equals",
                    "hashCode",
                    "toString",
                )
            ),
            FilterTestData(
                testName = "include hidden",
                configInline = { includeHidden = true },
                configContext = { includeHidden = true },
                type = getKType<TestClassWithMethods>(),
                members = listOf(
                    "someText",
                    "myFlag",
                    "isEnabled",
                    "hiddenField",
                    // "calculateValue",
                    // "compare",
                    // "isDisabled",
                    // "hiddenFunction",
                    // "equals",
                    // "hashCode",
                    // "toString",
                )
            ),
        )


    }

}