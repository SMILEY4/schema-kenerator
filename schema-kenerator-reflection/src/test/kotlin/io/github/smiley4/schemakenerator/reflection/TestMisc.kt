package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.testutils.ExpectedObjectTypeData
import io.github.smiley4.schemakenerator.testutils.ExpectedPropertyData
import io.github.smiley4.schemakenerator.testutils.ExpectedTypeParameterData
import io.github.smiley4.schemakenerator.testutils.shouldHaveExactly
import io.github.smiley4.schemakenerator.testutils.shouldMatch
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class TestMisc : StringSpec({

//    "test recursive" {
//        val context = TypeParserContext()
//        ReflectionTypeParser(context = context).parse<TestClassRecursiveGeneric<String>>()
//            .let { context.getData(it)!! }
//            .also { type ->
//                type.shouldMatch(
//                    ExpectedObjectTypeData(
//                        simpleName = "TestClassRecursiveGeneric",
//                        typeParameters = mapOf(
//                            "T" to ExpectedTypeParameterData(
//                                name = "T",
//                                typeId = "kotlin.String",
//                                nullable = false
//                            )
//                        ),
//                        supertypeIds = listOf(
//                            "kotlin.Any",
//                            "io.github.smiley4.schemakenerator.reflection.TestInterfaceRecursiveGeneric<io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<*>>"
//                        ),
//                        members = listOf(
//                            ExpectedPropertyData(
//                                name = "value",
//                                typeId = "kotlin.String"
//                            )
//                        )
//                    )
//                )
//            }
//        context shouldHaveExactly listOf(
//            "io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<kotlin.String>",
//            "io.github.smiley4.schemakenerator.reflection.TestInterfaceRecursiveGeneric<io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<*>>",
//            "io.github.smiley4.schemakenerator.reflection.TestClassRecursiveGeneric<*>",
//            "kotlin.String",
//            "kotlin.Any",
//            "*"
//        )
//    }
//
//    "test filters - default config" {
//        val context = TypeParserContext()
//        ReflectionTypeParser(
//            context = context,
//            config = {}
//        ).parse<TestClassWithMethods>()
//            .let { context.getData(it)!! }
//            .also { type ->
//                val members = (type as ObjectTypeData).members.map { it.name }
//                members shouldContainExactlyInAnyOrder listOf(
//                    "someText",
//                    "myFlag",
//                    "isEnabled",
//                    // "hiddenField",
//                    // "calculateValue",
//                    // "compare",
//                    // "isDisabled",
//                    // "hiddenFunction",
//                    // "equals",
//                    // "hashCode",
//                    // "toString",
//                )
//            }
//    }
//
//    "test filters - include getters" {
//        val context = TypeParserContext()
//        ReflectionTypeParser(
//            context = context,
//            config = {
//                includeGetters = true
//            }
//        ).parse<TestClassWithMethods>()
//            .let { context.getData(it)!! }
//            .also { type ->
//                val members = (type as ObjectTypeData).members.map { it.name }
//                members shouldContainExactlyInAnyOrder listOf(
//                    "someText",
//                    "myFlag",
//                    "isEnabled",
//                    // "hiddenField",
//                    // "calculateValue",
//                    // "compare",
//                    "isDisabled",
//                    // "hiddenFunction",
//                    // "equals",
//                    // "hashCode",
//                    // "toString",
//                )
//            }
//    }
//
//    "test filters - include weak getters" {
//        val context = TypeParserContext()
//        ReflectionTypeParser(
//            context = context,
//            config = {
//                includeWeakGetters = true
//            }
//        ).parse<TestClassWithMethods>()
//            .let { context.getData(it)!! }
//            .also { type ->
//                val members = (type as ObjectTypeData).members.map { it.name }
//                members shouldContainExactlyInAnyOrder listOf(
//                    "someText",
//                    "myFlag",
//                    "isEnabled",
//                    // "hiddenField",
//                    "calculateValue",
//                    // "compare",
//                    // "isDisabled",
//                    // "hiddenFunction",
//                    // "equals",
//                    "hashCode",
//                    "toString",
//                )
//            }
//    }
//
//    "test filters - include all getters" {
//        val context = TypeParserContext()
//        ReflectionTypeParser(
//            context = context,
//            config = {
//                includeGetters = true
//                includeWeakGetters = true
//            }
//        ).parse<TestClassWithMethods>()
//            .let { context.getData(it)!! }
//            .also { type ->
//                val members = (type as ObjectTypeData).members.map { it.name }
//                members shouldContainExactlyInAnyOrder listOf(
//                    "someText",
//                    "myFlag",
//                    "isEnabled",
//                    // "hiddenField",
//                    "calculateValue",
//                    // "compare",
//                    "isDisabled",
//                    // "hiddenFunction",
//                    // "equals",
//                    "hashCode",
//                    "toString",
//                )
//            }
//    }
//
//
//    "test filters - include hidden" {
//        val context = TypeParserContext()
//        ReflectionTypeParser(
//            context = context,
//            config = {
//                includeHidden = true
//            }
//        ).parse<TestClassWithMethods>()
//            .let { context.getData(it)!! }
//            .also { type ->
//                val members = (type as ObjectTypeData).members.map { it.name }
//                members shouldContainExactlyInAnyOrder listOf(
//                    "someText",
//                    "myFlag",
//                    "isEnabled",
//                    "hiddenField",
//                    // "calculateValue",
//                    // "compare",
//                    // "isDisabled",
//                    // "hiddenFunction",
//                    // "equals",
//                    // "hashCode",
//                    // "toString",
//                )
//            }
//    }

})