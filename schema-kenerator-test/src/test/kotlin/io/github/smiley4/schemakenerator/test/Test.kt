package io.github.smiley4.schemakenerator.test

import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileInlining
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.withAutoTitle
import io.swagger.v3.oas.models.media.Schema
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
class MyExampleClass(
    val someText: String,
    val someNullableInt: Int?,
    val someBoolList: List<Boolean>,
)

fun main() {

    val swaggerSchema: Schema<*> = typeOf<MyExampleClass>()
        .processKotlinxSerialization()
        .generateSwaggerSchema()
        .withAutoTitle(TitleType.SIMPLE)
        .compileInlining()
        .swagger

    println(swaggerSchema)

}