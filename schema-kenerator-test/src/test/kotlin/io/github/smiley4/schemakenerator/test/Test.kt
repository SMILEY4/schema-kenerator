@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.schemakenerator.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.schemakenerator.core.connectSubTypes
import io.github.smiley4.schemakenerator.core.data.Bundle
import io.github.smiley4.schemakenerator.jsonschema.compileInlining
import io.github.smiley4.schemakenerator.jsonschema.generateJsonSchema
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.kotest.core.spec.style.StringSpec
import io.swagger.v3.oas.models.media.Schema
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.reflect.typeOf

/**
 * internal / manual tests only
 */
class Test : StringSpec({

    "test" {
//        val result = Bundle(
//            data = typeOf<Parent>(),
//            supporting = listOf(
//                typeOf<ChildOne>(),
//                typeOf<ChildTwo>()
//            )
//        )
        val result = typeOf<Parent>()
            .processKotlinxSerialization()
            .connectSubTypes()
            .generateJsonSchema()
            .compileInlining()

        println(result.json.prettyPrint())

    }

}) {
    companion object {

        class SwaggerResult(
            val root: Schema<*>,
            val componentSchemas: Map<String, Schema<*>>
        )


        @Serializable
        @JsonClassDiscriminator("the_type")
        sealed class Parent(val common: Boolean)


        @Serializable
        @SerialName("child_one")
        data class ChildOne(val text: String) : Parent(false)


        @Serializable
        @SerialName("child_two")
        data class ChildTwo(val number: Int) : Parent(false)

        private val json = jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter()!!

    }
}