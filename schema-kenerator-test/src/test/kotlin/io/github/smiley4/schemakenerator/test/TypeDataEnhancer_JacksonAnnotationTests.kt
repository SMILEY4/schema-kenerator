//package io.github.smiley4.schemakenerator.test
//
//import com.fasterxml.jackson.annotation.JsonSubTypes
//import com.fasterxml.jackson.annotation.JsonTypeInfo
//import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
//import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
//import io.github.smiley4.schemakenerator.core.parser.idStr
//import io.github.smiley4.schemakenerator.core.parser.resolve
//import io.github.smiley4.schemakenerator.jackson.JacksonAnnotationTypeEnhancer
//import io.github.smiley4.schemakenerator.reflection.parsers.ReflectionTypeParser
//import io.kotest.core.spec.style.StringSpec
//import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
//
//@Suppress("ClassName")
//class TypeDataEnhancer_JacksonAnnotationTests : StringSpec({
//
//    "jackson-annotation enhancer" {
//        val context = TypeDataContext()
//        val parser = ReflectionTypeParser(context = context, config = { clearContext = false })
//
//        val superRef = parser.parse<TestSuperClass>()
//
//        JacksonAnnotationTypeEnhancer(parser).enhance(context)
//
//        superRef.resolve(context)!!
//            .let { superTypeData -> superTypeData as ObjectTypeData }
//            .also { superTypeData ->
//                superTypeData.subtypes.map { it.idStr() } shouldContainExactlyInAnyOrder listOf(
//                    TestSubClassA::class.qualifiedName!!,
//                    TestSubClassB::class.qualifiedName!!,
//                    TestSubClassC::class.qualifiedName!!
//                )
//            }
//    }
//
//    "jackson-annotation enhancer with generics" {
//        val context = TypeDataContext()
//        val parser = ReflectionTypeParser(context = context, config = { clearContext = false })
//
//        val superRef = parser.parse<GenericTestSuperClass<*>>()
//
//        JacksonAnnotationTypeEnhancer(parser).enhance(context)
//
//        superRef.resolve(context)!!
//            .let { superTypeData -> superTypeData as ObjectTypeData }
//            .also { superTypeData ->
//                superTypeData.subtypes.map { it.idStr() } shouldContainExactlyInAnyOrder listOf(
//                    GenericTestSubClassA::class.qualifiedName!!,
//                    GenericTestSubClassB::class.qualifiedName!!,
//                    GenericTestSubClassC::class.qualifiedName!!
//                )
//            }
//    }
//
//}) {
//    companion object {
//
//        @JsonTypeInfo(
//            use = JsonTypeInfo.Id.NAME,
//            include = JsonTypeInfo.As.PROPERTY,
//            property = "type"
//        )
//        @JsonSubTypes(
//            JsonSubTypes.Type(value = TestSubClassA::class, name = "a"),
//            JsonSubTypes.Type(value = TestSubClassB::class, name = "b"),
//            JsonSubTypes.Type(value = TestSubClassC::class, name = "c"),
//        )
//        private open class TestSuperClass
//
//        private class TestSubClassA : TestSuperClass()
//        private class TestSubClassB : TestSuperClass()
//        private class TestSubClassC : TestSuperClass()
//
//
//
//        @JsonTypeInfo(
//            use = JsonTypeInfo.Id.NAME,
//            include = JsonTypeInfo.As.PROPERTY,
//            property = "type"
//        )
//        @JsonSubTypes(
//            JsonSubTypes.Type(value = GenericTestSubClassA::class, name = "a"),
//            JsonSubTypes.Type(value = GenericTestSubClassB::class, name = "b"),
//            JsonSubTypes.Type(value = GenericTestSubClassC::class, name = "c"),
//        )
//        private open class GenericTestSuperClass<T>(val someValue: T)
//
//        private class GenericTestSubClassA : GenericTestSuperClass<String>("test")
//        private class GenericTestSubClassB : GenericTestSuperClass<Int>(42)
//        private class GenericTestSubClassC : GenericTestSuperClass<Float>(4.2f)
//
//    }
//}