//package io.github.smiley4.schemakenerator.swagger.module
//
//import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
//import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
//import io.github.smiley4.schemakenerator.swagger.SwaggerSchema
//import io.github.smiley4.schemakenerator.swagger.getDefinition
//import io.swagger.v3.oas.models.media.Schema
//import kotlin.reflect.KClass
//
///**
// * Abstract class for processing annotations
// */
//abstract class AbstractAnnotationsModule : SwaggerSchemaGeneratorModule {
//
//    /**
//     * @param schema the [SwaggerSchema] to process
//     * @param typeData the [BaseTypeData] associated with the schema
//     * @param type the annotation to process
//     * @param key the name of the field of the annotation to process; set to null to ignore fields, continues with 'null'
//     * @param processProperties whether to also process properties/members of the schema/typeData
//     * @param valueTransformer convert the generic value of the annotation into a more usable type
//     * @param action called for each annotation with the transformed value of the annotation-field and the corresponding schema
//     */
//    protected fun <T> processAnnotation(
//        schema: SwaggerSchema,
//        typeData: BaseTypeData,
//        type: KClass<*>,
//        key: String?,
//        processProperties: Boolean,
//        valueTransformer: (value: Any?) -> T,
//        action: (typeSchema: Schema<*>, value: T) -> Unit
//    ) {
//        val definition = schema.getDefinition()
//        // object
//        typeData.annotations
//            .filter { it.name == type.qualifiedName }
//            .map { key?.let { k -> it.values[k] } }
//            .map { valueTransformer(it) }
//            .forEach { action(definition, it) }
//
//        // properties
//        if(processProperties && typeData is ObjectTypeData) {
//            typeData.members.forEach { member ->
//                definition.properties[member.name]?.also { property ->
//                    member.annotations
//                        .filter { it.name == type.qualifiedName }
//                        .map { key?.let { k -> it.values[k] } }
//                        .map { valueTransformer(it) }
//                        .forEach { action(property, it) }
//                }
//            }
//        }
//    }
//
//}