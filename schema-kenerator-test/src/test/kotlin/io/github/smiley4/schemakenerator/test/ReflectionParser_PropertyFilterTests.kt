package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerator
import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.module.InliningGenerator
import io.github.smiley4.schemakenerator.reflection.getKType
import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.reflect.KType

@Suppress("ClassName")
class ReflectionParser_PropertyFilterTests : FunSpec({

    context("filter properties") {
        withData(TEST_DATA) { data ->
            val context = TypeDataContext()
            val resultParser = ReflectionTypeParser(context = context, config = {
                inline = true
                includeGetters = data.includeGetters
                includeWeakGetters = data.includeWeakGetters
                includeHidden = data.includeHidden
            }).parse(data.type)
            val generatorResult = JsonSchemaGenerator()
                .withModule(InliningGenerator())
                .generate(resultParser, context)
            (generatorResult.schema.properties["properties"] as JsonObject).properties.keys shouldContainExactlyInAnyOrder data.expectedProperties
        }
    }

}) {
    companion object {

        private class FilterableClassPublicGetters(
            val publicField: String,
            private val privateField: String,
            val isPublicFlag: Boolean,
            private val isPrivateFlag: Boolean
        ) {
            @JvmName("getter_publicField")
            fun getPublicField() = publicField
            @JvmName("getter_privateField")
            fun getPrivateField() = privateField
            @JvmName("getter_publicFlag")
            fun isPublicFlag() = isPublicFlag
            @JvmName("getter_privateFlag")
            fun isPrivateFlag() = isPrivateFlag
            fun getOtherValue() = "hello"
            fun doSomethingWithReturn() = 42
            private fun doSomethingWithReturnPrivate() = 42
            fun doSomethingNoReturn() = Unit
            fun getCalculatedValue(a: Int, b: Int) = a + b
        }

        private class TestData(
            val type: KType,
            val includeGetters: Boolean,
            val includeWeakGetters: Boolean,
            val includeHidden: Boolean,
            val expectedProperties: List<String>
        ) : WithDataTestName {
            override fun dataTestName() =
                "${if (includeGetters) "with" else "without"} getters, ${if (includeWeakGetters) "with" else "without"} weak-getters, ${if (includeHidden) "with" else "without"} hidden"
        }

        private val TEST_DATA = listOf(
            TestData(
                type = getKType<FilterableClassPublicGetters>(),
                includeGetters = false,
                includeWeakGetters = false,
                includeHidden = false,
                expectedProperties = listOf(
                    // "privateField",
                     "publicField",
                    // "isPrivateFlag",
                     "isPublicFlag",
                    // "getOtherValue",
                    // "getPrivateField",
                    // "getPublicField",
                    // "doSomethingNoReturn",
                    // "doSomethingWithReturn",
                    // "doSomethingWithReturnPrivate",
                    // "hashCode",
                    // "toString"
                )
            ),
            TestData(
                type = getKType<FilterableClassPublicGetters>(),
                includeGetters = true,
                includeWeakGetters = false,
                includeHidden = false,
                expectedProperties = listOf(
                    // "privateField",
                     "publicField",
                     "isPrivateFlag",
                     "isPublicFlag",
                     "getOtherValue",
                     "getPrivateField",
                     "getPublicField",
                    // "doSomethingNoReturn",
                    // "doSomethingWithReturn",
                    // "doSomethingWithReturnPrivate",
                    // "hashCode",
                    // "toString"
                )
            ),
            TestData(
                type = getKType<FilterableClassPublicGetters>(),
                includeGetters = false,
                includeWeakGetters = true,
                includeHidden = false,
                expectedProperties = listOf(
                    // "privateField",
                     "publicField",
                    // "isPrivateFlag",
                     "isPublicFlag",
                    // "getOtherValue",
                    // "getPrivateField",
                    // "getPublicField",
                     "doSomethingNoReturn",
                     "doSomethingWithReturn",
                    // "doSomethingWithReturnPrivate",
                     "hashCode",
                     "toString"
                )
            ),
            TestData(
                type = getKType<FilterableClassPublicGetters>(),
                includeGetters = true,
                includeWeakGetters = true,
                includeHidden = false,
                expectedProperties = listOf(
                    // "privateField",
                     "publicField",
                     "isPrivateFlag",
                     "isPublicFlag",
                     "getOtherValue",
                     "getPrivateField",
                     "getPublicField",
                     "doSomethingNoReturn",
                     "doSomethingWithReturn",
                    // "doSomethingWithReturnPrivate",
                     "hashCode",
                     "toString"
                )
            ),
            TestData(
                type = getKType<FilterableClassPublicGetters>(),
                includeGetters = false,
                includeWeakGetters = false,
                includeHidden = true,
                expectedProperties = listOf(
                     "privateField",
                     "publicField",
                     "isPrivateFlag",
                     "isPublicFlag",
                    // "getOtherValue",
                    // "getPrivateField",
                    // "getPublicField",
                    // "doSomethingNoReturn",
                    // "doSomethingWithReturn",
                    // "doSomethingWithReturnPrivate",
                    // "hashCode",
                    // "toString"
                )
            ),
            TestData(
                type = getKType<FilterableClassPublicGetters>(),
                includeGetters = true,
                includeWeakGetters = false,
                includeHidden = true,
                expectedProperties = listOf(
                     "privateField",
                     "publicField",
                     "isPrivateFlag",
                     "isPublicFlag",
                     "getOtherValue",
                     "getPrivateField",
                     "getPublicField",
                    // "doSomethingNoReturn",
                    // "doSomethingWithReturn",
                    // "doSomethingWithReturnPrivate",
                    // "hashCode",
                    // "toString"
                )
            ),
            TestData(
                type = getKType<FilterableClassPublicGetters>(),
                includeGetters = true,
                includeWeakGetters = true,
                includeHidden = true,
                expectedProperties = listOf(
                    "privateField",
                    "publicField",
                    "isPrivateFlag",
                    "isPublicFlag",
                    "getOtherValue",
                    "getPrivateField",
                    "getPublicField",
                    "doSomethingNoReturn",
                    "doSomethingWithReturn",
                    "doSomethingWithReturnPrivate",
                    "hashCode",
                    "toString"
                )
            )
        )

    }
}