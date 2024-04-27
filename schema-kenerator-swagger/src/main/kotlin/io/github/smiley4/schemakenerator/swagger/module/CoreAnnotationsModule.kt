//package io.github.smiley4.schemakenerator.swagger.module
//
//import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
//import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
//import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
//import io.github.smiley4.schemakenerator.core.annotations.SchemaExample
//import io.github.smiley4.schemakenerator.core.annotations.SchemaTitle
//import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
//import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
//import io.github.smiley4.schemakenerator.swagger.SwaggerSchema
//import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerator
//import io.swagger.v3.oas.models.media.Schema
//
///**
// * Processes schema-kenerator core-annotations and appends metadata to the schema
// * - title from [SchemaTitle]
// * - description from [SchemaDescription]
// * - default value from [SchemaDefault]
// * - examples from [SchemaExample]
// * - deprecated from [SchemaDeprecated]
// */
//class CoreAnnotationsModule : AbstractAnnotationsModule() {
//
//    override fun build(generator: SwaggerSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, depth: Int): SwaggerSchema? =
//        null
//
//    override fun enhance(
//        generator: SwaggerSchemaGenerator,
//        context: TypeDataContext,
//        typeData: BaseTypeData,
//        schema: SwaggerSchema,
//        depth: Int
//    ) {
//        processAnnotation(
//            schema = schema,
//            typeData = typeData,
//            type = SchemaTitle::class,
//            key = "title",
//            processProperties = false,
//            valueTransformer = { value -> value?.let { it as String } },
//            action = { typeSchema, value -> typeSchema.title = value }
//        )
//        processAnnotation(
//            schema = schema,
//            typeData = typeData,
//            type = SchemaDescription::class,
//            key = "description",
//            processProperties = true,
//            valueTransformer = { value -> value?.let { it as String } },
//            action = { typeSchema, value -> typeSchema.description = value }
//        )
//        processAnnotation(
//            schema = schema,
//            typeData = typeData,
//            type = SchemaDefault::class,
//            key = "default",
//            processProperties = true,
//            valueTransformer = { value -> value?.let { it as String } },
//            action = { typeSchema, value -> typeSchema.setDefault(value) }
//        )
//        processAnnotation(
//            schema = schema,
//            typeData = typeData,
//            type = SchemaExample::class,
//            key = "example",
//            processProperties = true,
//            valueTransformer = { value -> value?.let { it as String } },
//            action = { typeSchema, value ->
//                @Suppress("UNCHECKED_CAST")
//                (typeSchema as Schema<Any?>).addExample(value)
//            }
//        )
//        processAnnotation(
//            schema = schema,
//            typeData = typeData,
//            type = SchemaDeprecated::class,
//            key = "deprecated",
//            processProperties = true,
//            valueTransformer = { value -> value?.let { it as Boolean } },
//            action = { typeSchema, value -> typeSchema.deprecated = value }
//        )
//    }
//
//}