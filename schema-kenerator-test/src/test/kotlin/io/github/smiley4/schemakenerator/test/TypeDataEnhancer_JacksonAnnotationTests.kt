package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.kotest.core.spec.style.StringSpec

@Suppress("ClassName")
class TypeDataEnhancer_JacksonAnnotationTests : StringSpec({

    "test" {
        TODO()
    }

}) {
    companion object {

        @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type"
        )
        @JsonSubTypes(
            JsonSubTypes.Type(value = TestSubClassA::class, name = "a"),
            JsonSubTypes.Type(value = TestSubClassB::class, name = "b"),
            JsonSubTypes.Type(value = TestSubClassC::class, name = "c"),
        )
        private open class TestSuperClass(val fieldSuper: String)

        private class TestSubClassA(val a: String, fieldSuper: String) : TestSuperClass(fieldSuper)
        private class TestSubClassB(val b: String, fieldSuper: String) : TestSuperClass(fieldSuper)
        private class TestSubClassC(val c: String, fieldSuper: String) : TestSuperClass(fieldSuper)

    }
}