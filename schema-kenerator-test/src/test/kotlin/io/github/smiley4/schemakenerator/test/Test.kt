package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonSubTypes
import io.github.smiley4.schemakenerator.core.connectSubTypes
import io.github.smiley4.schemakenerator.jackson.collectJacksonSubTypes
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.reflection.processReflection
import io.github.smiley4.schemakenerator.test.models.reflection.ClassWithNestedClass
import io.kotest.core.spec.style.StringSpec
import kotlin.reflect.typeOf

class Test : StringSpec({

    "test 1" {

        val result = typeOf<ClassWithNestedClass>()
            .collectJacksonSubTypes(typeProcessing = { type -> type.processReflection() })
            .processReflection()
            .generateJsonSchema()
            .compileInlining()

        // todo: should only be a single result instead of one for each used type
//        result shouldHaveSize  1
        println(result)

    }

    "test 2" {

        val result = typeOf<JacksonBaseClass>()
            .collectJacksonSubTypes(typeProcessing = { type -> type.processReflection() })
            .processReflection()
            .connectSubTypes()
            .generateJsonSchema()
            .compileInlining()

        // todo: should also only be a single result instead of one for each used type
//        result shouldHaveSize  1
        println(result)

    }

}) {
    companion object {

        @JsonSubTypes(
            JsonSubTypes.Type(value = JacksonSubClassA::class),
            JsonSubTypes.Type(value = JacksonSubClassB::class),
        )
        private open class JacksonBaseClass
        private class JacksonSubClassA(val someValue: String) : JacksonBaseClass()
        private class JacksonSubClassB(val someValue: Int) : JacksonBaseClass()

    }
}