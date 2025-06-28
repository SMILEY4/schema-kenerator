package io.github.smiley4.schemakenerator.test.cases

import io.github.smiley4.schemakenerator.core.data.TypeDataGroup
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps
import io.github.smiley4.schemakenerator.jsonschema.data.IntermediateJsonSchemaData
import io.github.smiley4.schemakenerator.swagger.SwaggerSteps
import io.github.smiley4.schemakenerator.swagger.data.IntermediateSwaggerSchemaData
import io.kotest.datatest.WithDataTestName
import kotlin.reflect.KType
import io.github.smiley4.schemakenerator.jsonschema.data.RefType as JsonRefType
import io.github.smiley4.schemakenerator.swagger.data.RefType as SwaggerRefType

class TestCase(
    val name: String,
    val group: String,
    val type: KType,
    val withReflection: Boolean,
    val withKotlinxSerialization: Boolean,
    val swaggerGeneratorConfig: SwaggerSteps.SwaggerSchemaGenerationStepConfig.() -> Unit,
    val jsonGeneratorConfig: JsonSchemaSteps.JsonSchemaGenerationStepConfig.() -> Unit,
    val postAnalyze: TypeDataGroup.() -> TypeDataGroup,
    val postGenerateSwaggerSchema: IntermediateSwaggerSchemaData.() -> IntermediateSwaggerSchemaData,
    val postGenerateJsonSchema: IntermediateJsonSchemaData.() -> IntermediateJsonSchemaData,
    val swaggerRefType: SwaggerRefType,
    val jsonRefType: JsonRefType,
    val expectedSwaggerInline: String?,
    val expectedSwaggerReference: String?,
    val expectedJsonInline: String?,
    val expectedJsonReference: String?,
    val expectedSwaggerInlineKotlinxSerialization: String?,
    val expectedSwaggerReferenceKotlinxSerialization: String?,
    val expectedJsonInlineKotlinxSerialization: String?,
    val expectedJsonReferenceKotlinxSerialization: String?,
) : WithDataTestName {
    override fun dataTestName() = name
}

fun case(group: String, name: String, builder: TestCaseBuilder.() -> Unit = {}): TestCase {
    return TestCaseBuilder()
        .apply(builder)
        .also {
            it.name = name
            it.group = group
        }
        .build()
}

class TestCaseBuilder {

    var name: String? = null
    var group: String? = null

    var type: KType? = null

    var withReflection: Boolean = true
    var withKotlinxSerialization: Boolean = true

    var swaggerConfig: SwaggerSteps.SwaggerSchemaGenerationStepConfig.() -> Unit = {}
    var jsonConfig: JsonSchemaSteps.JsonSchemaGenerationStepConfig.() -> Unit = {}

    var postAnalyze: TypeDataGroup.() -> TypeDataGroup = { this }

    var postGenerateSwaggerSchema: IntermediateSwaggerSchemaData.() -> IntermediateSwaggerSchemaData = { this }
    var postGenerateJsonSchema: IntermediateJsonSchemaData.() -> IntermediateJsonSchemaData = { this }

    var swaggerRefType: SwaggerRefType = SwaggerRefType.OPENAPI_SIMPLE
    var jsonRefType: JsonRefType = JsonRefType.SIMPLE

    var expectedSwagger: String? = null
    var expectedSwaggerInline: String? = null
    var expectedSwaggerReference: String? = null

    var expectedSwaggerKotlinxSerialization: String? = null
    var expectedSwaggerInlineKotlinxSerialization: String? = null
    var expectedSwaggerReferenceKotlinxSerialization: String? = null

    var expectedJson: String? = null
    var expectedJsonInline: String? = null
    var expectedJsonReference: String? = null

    var expectedJsonKotlinxSerialization: String? = null
    var expectedJsonInlineKotlinxSerialization: String? = null
    var expectedJsonReferenceKotlinxSerialization: String? = null

    fun build(): TestCase {
        return TestCase(
            name = name!!,
            group = group!!,
            type = type!!,
            withReflection = withReflection,
            withKotlinxSerialization = withKotlinxSerialization,
            swaggerGeneratorConfig = swaggerConfig,
            jsonGeneratorConfig = jsonConfig,
            postAnalyze = postAnalyze,
            postGenerateSwaggerSchema = postGenerateSwaggerSchema,
            postGenerateJsonSchema = postGenerateJsonSchema,
            swaggerRefType = swaggerRefType,
            jsonRefType = jsonRefType,
            expectedSwaggerInline = expectedSwaggerInline ?: expectedSwagger,
            expectedSwaggerReference = expectedSwaggerReference ?: expectedSwagger,
            expectedJsonInline = expectedJsonInline ?: expectedJson,
            expectedJsonReference = expectedJsonReference ?: expectedJson,
            expectedSwaggerInlineKotlinxSerialization = expectedSwaggerInlineKotlinxSerialization ?: expectedSwaggerKotlinxSerialization,
            expectedSwaggerReferenceKotlinxSerialization = expectedSwaggerReferenceKotlinxSerialization ?: expectedSwaggerKotlinxSerialization,
            expectedJsonInlineKotlinxSerialization = expectedJsonInlineKotlinxSerialization ?: expectedJsonKotlinxSerialization,
            expectedJsonReferenceKotlinxSerialization = expectedJsonReferenceKotlinxSerialization ?: expectedJsonKotlinxSerialization,
        )
    }

}
