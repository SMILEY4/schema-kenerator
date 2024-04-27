//package io.github.smiley4.schemakenerator.jackson
//
//import com.fasterxml.jackson.annotation.JsonSubTypes
//import io.github.smiley4.schemakenerator.core.enhancer.TypeDataEnhancer
//import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
//import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
//import io.github.smiley4.schemakenerator.core.parser.TypeParser
//import io.github.smiley4.schemakenerator.core.parser.TypeRef
//import kotlin.reflect.full.createType
//
//
///**
// * Detects and adds missing supertype-subtype-relations between type-data in the provided context.
// * Type-data not present in the context is not included.
// */
//class JacksonAnnotationTypeEnhancer(val typeParser: TypeParser<*>) : TypeDataEnhancer {
//
//    override fun enhance(context: TypeDataContext) {
//        context.getTypes()
//            .filterIsInstance<ObjectTypeData>()
//            .forEach { it.subtypes.addAll(getJsonSubTypes(it)) }
//    }
//
//    private fun getJsonSubTypes(typeData: ObjectTypeData): List<TypeRef> {
//        @Suppress("UNCHECKED_CAST")
//        return typeData.annotations
//            .find { it.name == JsonSubTypes::class.qualifiedName!! }
//            ?.let { it.values["value"] as Array<JsonSubTypes.Type> }
//            ?.let { it.map { v -> v.value } }
//            ?.map { typeParser.parse(it.createType()) }
//            ?: emptyList()
//    }
//
//}