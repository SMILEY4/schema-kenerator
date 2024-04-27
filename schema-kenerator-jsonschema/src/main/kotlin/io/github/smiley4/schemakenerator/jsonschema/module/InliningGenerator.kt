//package io.github.smiley4.schemakenerator.jsonschema.module
//
//import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
//import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
//import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
//import io.github.smiley4.schemakenerator.core.parser.MapTypeData
//import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
//import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
//import io.github.smiley4.schemakenerator.core.parser.PropertyData
//import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
//import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
//import io.github.smiley4.schemakenerator.core.parser.resolve
//import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchema
//import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaGenerator
//import io.github.smiley4.schemakenerator.jsonschema.asJson
//import io.github.smiley4.schemakenerator.jsonschema.json.JsonNode
//import io.github.smiley4.schemakenerator.jsonschema.schema.JsonSchemaUtils
//
///**
// * Generates the base json-schema while inlining all definitions
// */
//class InliningGenerator : JsonSchemaGeneratorModule {
//
//    private val schema = JsonSchemaUtils()
//
//    override fun build(generator: JsonSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, depth: Int): JsonSchema {
//        if (typeData is ObjectTypeData && typeData.subtypes.isNotEmpty()) {
//            return buildWithSubtypes(generator, typeData, context, depth)
//        }
//        return when (typeData) {
//            is PrimitiveTypeData -> buildPrimitiveSchema(typeData)
//            is EnumTypeData -> buildEnumSchema(typeData)
//            is CollectionTypeData -> buildCollectionSchema(generator, typeData, context, depth)
//            is MapTypeData -> buildMapSchema(generator, typeData, context, depth)
//            is ObjectTypeData -> buildObjectSchema(generator, typeData, context, depth)
//            is WildcardTypeData -> buildAnySchema()
//            else -> JsonSchema(schema.nullSchema())
//        }
//    }
//
//
//    override fun enhance(
//        generator: JsonSchemaGenerator,
//        context: TypeDataContext,
//        typeData: BaseTypeData,
//        schema: JsonSchema,
//        depth: Int
//    ) = Unit
//
//
//    private fun buildWithSubtypes(
//        generator: JsonSchemaGenerator,
//        typeData: ObjectTypeData,
//        context: TypeDataContext,
//        depth: Int
//    ): JsonSchema {
//        return JsonSchema(schema.subtypesSchema(typeData.subtypes.map { subtype ->
//            generator.generate(subtype, context, depth + 1).asJson()
//        }))
//    }
//
//
//    private fun buildEnumSchema(typeData: EnumTypeData): JsonSchema {
//        return JsonSchema(schema.enumSchema(typeData.enumConstants))
//    }
//
//
//    private fun buildObjectSchema(
//        generator: JsonSchemaGenerator,
//        typeData: ObjectTypeData,
//        context: TypeDataContext,
//        depth: Int
//    ): JsonSchema {
//        val requiredProperties = mutableSetOf<String>()
//        val propertySchemas = mutableMapOf<String, JsonNode>()
//
//        collectMembers(typeData, context).forEach { member ->
//            val memberSchema = generator.generate(member.type, context, depth + 1).asJson()
//            propertySchemas[member.name] = memberSchema
//            if (!member.nullable) {
//                requiredProperties.add(member.name)
//            }
//        }
//
//        return JsonSchema(schema.objectSchema(propertySchemas, requiredProperties))
//    }
//
//
//    private fun collectMembers(typeData: ObjectTypeData, context: TypeDataContext): List<PropertyData> {
//        return buildList {
//            addAll(typeData.members)
//            typeData.supertypes.forEach { supertypeRef ->
//                val supertype = supertypeRef.resolve(context)
//                if (supertype is ObjectTypeData) {
//                    addAll(collectMembers(supertype, context))
//                }
//            }
//        }
//    }
//
//
//    private fun buildCollectionSchema(
//        generator: JsonSchemaGenerator,
//        typeData: CollectionTypeData,
//        context: TypeDataContext,
//        depth: Int
//    ): JsonSchema {
//        val itemSchema = generator.generate(typeData.itemType.type, context, depth + 1).asJson()
//        return JsonSchema(
//            schema.arraySchema(
//                items = itemSchema,
//            )
//        )
//    }
//
//
//    private fun buildMapSchema(generator: JsonSchemaGenerator, typeData: MapTypeData, context: TypeDataContext, depth: Int): JsonSchema {
//        val valueSchema = generator.generate(typeData.valueType.type, context, depth + 1).asJson()
//        return JsonSchema(schema.mapObjectSchema(valueSchema))
//    }
//
//
//    private fun buildAnySchema(): JsonSchema {
//        return JsonSchema(schema.anyObjectSchema())
//    }
//
//    private fun buildPrimitiveSchema(typeData: PrimitiveTypeData): JsonSchema {
//        return when (typeData.qualifiedName) {
//            Number::class.qualifiedName -> schema.numericSchema(
//                integer = false,
//                min = null,
//                max = null,
//            )
//            Byte::class.qualifiedName -> schema.numericSchema(
//                integer = true,
//                min = Byte.MIN_VALUE,
//                max = Byte.MAX_VALUE,
//            )
//            Short::class.qualifiedName -> schema.numericSchema(
//                integer = true,
//                min = Short.MIN_VALUE,
//                max = Short.MAX_VALUE,
//            )
//            Int::class.qualifiedName -> schema.numericSchema(
//                integer = true,
//                min = Int.MIN_VALUE,
//                max = Int.MAX_VALUE,
//            )
//            Long::class.qualifiedName -> schema.numericSchema(
//                integer = true,
//                min = Long.MIN_VALUE,
//                max = Long.MAX_VALUE,
//            )
//            UByte::class.qualifiedName -> schema.numericSchema(
//                integer = true,
//                min = UByte.MIN_VALUE.toLong(),
//                max = UByte.MAX_VALUE.toLong(),
//            )
//            UShort::class.qualifiedName -> schema.numericSchema(
//                integer = true,
//                min = UShort.MIN_VALUE.toLong(),
//                max = UShort.MAX_VALUE.toLong(),
//            )
//            UInt::class.qualifiedName -> schema.numericSchema(
//                integer = true,
//                min = UInt.MIN_VALUE.toLong(),
//                max = UInt.MAX_VALUE.toLong(),
//            )
//            ULong::class.qualifiedName -> schema.numericSchema(
//                integer = true,
//                min = null,
//                max = null,
//            )
//            Float::class.qualifiedName -> schema.numericSchema(
//                integer = false,
//                min = Float.MIN_VALUE,
//                max = Float.MAX_VALUE,
//            )
//            Double::class.qualifiedName -> schema.numericSchema(
//                integer = false,
//                min = Double.MIN_VALUE,
//                max = Double.MAX_VALUE,
//            )
//            Boolean::class.qualifiedName -> schema.booleanSchema()
//            Char::class.qualifiedName -> schema.stringSchema(
//                min = 1,
//                max = 1,
//            )
//            String::class.qualifiedName -> schema.stringSchema(
//                min = null,
//                max = null,
//            )
//            Any::class.qualifiedName -> schema.anyObjectSchema()
//            Unit::class.qualifiedName -> schema.nullSchema()
//            else -> schema.nullSchema()
//        }.let {
//            JsonSchema(it)
//        }
//    }
//
//}