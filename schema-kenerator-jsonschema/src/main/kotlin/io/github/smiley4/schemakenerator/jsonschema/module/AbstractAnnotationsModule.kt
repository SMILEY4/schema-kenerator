//package io.github.smiley4.schemakenerator.jsonschema.module
//
//import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
//import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
//import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchema
//import io.github.smiley4.schemakenerator.jsonschema.getDefinition
//import io.github.smiley4.schemakenerator.jsonschema.json.JsonObject
//import kotlin.reflect.KClass
//
///**
// * Abstract class for processing annotations
// */
//abstract class AbstractAnnotationsModule : JsonSchemaGeneratorModule {
//
//    /**
//     * @param schema the [JsonSchema] to process
//     * @param typeData the [BaseTypeData] associated with the schema
//     * @param type the annotation to process
//     * @param key the name of the field of the annotation to process; set to null to ignore fields, continues with 'null'
//     * @param processProperties whether to also process properties/members of the schema/typeData
//     * @param valueTransformer convert the generic value of the annotation into a more usable type
//     * @param action called for each annotation with the transformed value of the annotation-field and the corresponding schema
//     */
//    protected fun <T> processAnnotation(
//        schema: JsonSchema,
//        typeData: BaseTypeData,
//        type: KClass<*>,
//        key: String?,
//        processProperties: Boolean,
//        valueTransformer: (value: Any?) -> T,
//        action: (typeSchema: JsonObject, value: T) -> Unit
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
//        if (processProperties && typeData is ObjectTypeData) {
//            typeData.members.forEach { member ->
//                (definition.properties["properties"] as JsonObject).properties[member.name]?.also { property ->
//                    if (property is JsonObject) {
//                        member.annotations
//                            .filter { it.name == type.qualifiedName }
//                            .map { key?.let { k -> it.values[k] } }
//                            .map { valueTransformer(it) }
//                            .forEach { action(property, it) }
//                    }
//                }
//            }
//        }
//    }
//
//}