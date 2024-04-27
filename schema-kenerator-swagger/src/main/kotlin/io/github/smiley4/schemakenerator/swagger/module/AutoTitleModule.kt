//package io.github.smiley4.schemakenerator.swagger.module
//
//import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
//import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
//import io.github.smiley4.schemakenerator.core.parser.resolve
//import io.github.smiley4.schemakenerator.swagger.SwaggerSchema
//import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerator
//import io.github.smiley4.schemakenerator.swagger.getDefinition
//
///**
// * Automatically detects and appends a title to the generated schema
// */
//class AutoTitleModule(private val type: AutoTitleType = AutoTitleType.SIMPLE_NAME) : SwaggerSchemaGeneratorModule {
//
//    companion object {
//        enum class AutoTitleType {
//            NONE,
//            SIMPLE_NAME,
//            QUALIFIED_NAME
//        }
//    }
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
//        val definition = schema.getDefinition()
//        buildObjectTitle(context, typeData)?.also { title ->
//            definition.title = title
//        }
//    }
//
//    private fun buildObjectTitle(context: TypeDataContext, typeData: BaseTypeData): String? {
//        return when (type) {
//            AutoTitleType.NONE -> null
//            AutoTitleType.SIMPLE_NAME -> buildMergedTitle(context, typeData) { it.simpleName }
//            AutoTitleType.QUALIFIED_NAME -> buildMergedTitle(context, typeData) { it.qualifiedName }
//        }
//    }
//
//    private fun buildMergedTitle(
//        context: TypeDataContext,
//        typeData: BaseTypeData,
//        titleProvider: (typeData: BaseTypeData) -> String
//    ): String {
//        return buildString {
//            append(titleProvider(typeData))
//            if (typeData.typeParameters.isNotEmpty()) {
//                append("<")
//                typeData.typeParameters
//                    .mapNotNull { (_, typeParam) -> typeParam.type.resolve(context) }
//                    .joinToString(",") { titleProvider(it) }
//                    .also { append(it) }
//                append(">")
//            }
//        }
//    }
//
//}