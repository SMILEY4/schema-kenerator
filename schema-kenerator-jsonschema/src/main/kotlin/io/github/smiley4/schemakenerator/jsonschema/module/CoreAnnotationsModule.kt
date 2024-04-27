//package io.github.smiley4.schemakenerator.jsonschema.module
//
//import io.github.smiley4.schemakenerator.core.annotations.SchemaDefault
//import io.github.smiley4.schemakenerator.core.annotations.SchemaDeprecated
//import io.github.smiley4.schemakenerator.core.annotations.SchemaDescription
//import io.github.smiley4.schemakenerator.core.annotations.SchemaExample
//import io.github.smiley4.schemakenerator.core.annotations.SchemaTitle
//import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
//import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
//import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchema
//import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerator
//import io.github.smiley4.schemakenerator.jsonschema.json.JsonArray
//import io.github.smiley4.schemakenerator.jsonschema.json.JsonBooleanValue
//import io.github.smiley4.schemakenerator.jsonschema.json.JsonTextValue
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
//    override fun build(generator: JsonSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, depth: Int): JsonSchema? =
//        null
//
//    override fun enhance(
//        generator: JsonSchemaGenerator,
//        context: TypeDataContext,
//        typeData: BaseTypeData,
//        schema: JsonSchema,
//        depth: Int
//    ) {
//        processAnnotation(
//            schema = schema,
//            typeData = typeData,
//            type = SchemaTitle::class,
//            key = "title",
//            processProperties = false,
//            valueTransformer = { value -> value?.let { it as String } },
//            action = { typeSchema, value -> value?.also { typeSchema.properties["title"] = JsonTextValue(it) } }
//        )
//        processAnnotation(
//            schema = schema,
//            typeData = typeData,
//            type = SchemaDescription::class,
//            key = "description",
//            processProperties = true,
//            valueTransformer = { value -> value?.let { it as String } },
//            action = { typeSchema, value -> value?.also { typeSchema.properties["description"] = JsonTextValue(it) } }
//        )
//        processAnnotation(
//            schema = schema,
//            typeData = typeData,
//            type = SchemaDefault::class,
//            key = "default",
//            processProperties = true,
//            valueTransformer = { value -> value?.let { it as String } },
//            action = { typeSchema, value -> value?.also { typeSchema.properties["default"] = JsonTextValue(it) } }
//        )
//        processAnnotation(
//            schema = schema,
//            typeData = typeData,
//            type = SchemaExample::class,
//            key = "example",
//            processProperties = true,
//            valueTransformer = { value -> value?.let { it as String } },
//            action = { typeSchema, value ->
//                value?.also {
//                    if (!typeSchema.properties.containsKey("examples")) {
//                        typeSchema.properties["examples"] = JsonArray()
//                    }
//                    (typeSchema.properties["examples"] as JsonArray).items.add(JsonTextValue(it))
//                }
//            }
//        )
//        processAnnotation(
//            schema = schema,
//            typeData = typeData,
//            type = SchemaDeprecated::class,
//            key = "deprecated",
//            processProperties = true,
//            valueTransformer = { value -> value?.let { it as Boolean } },
//            action = { typeSchema, value -> value?.also { typeSchema.properties["deprecated"] = JsonBooleanValue(it) } }
//        )
//    }
//
//}