package io.github.smiley4.schemakenerator.swagger.module

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.PropertyData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.swagger.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerator
import io.github.smiley4.schemakenerator.swagger.swagger.SwaggerSchemaUtils
import io.swagger.v3.oas.models.media.Schema
import java.math.BigDecimal

/**
 * Generates the base swagger-schema while referencing all definitions
 * @param referenceRoot whether to also reference the root definition
 */
class ReferencingGenerator(val referenceRoot: Boolean = true) : SwaggerSchemaGeneratorModule {

    private val schema = SwaggerSchemaUtils()

    override fun build(generator: SwaggerSchemaGenerator, context: TypeDataContext, typeData: BaseTypeData, depth: Int): SwaggerSchema {
        if (typeData is ObjectTypeData && typeData.subtypes.isNotEmpty()) {
            return buildWithSubtypes(generator, typeData, context, depth)
        }
        return when (typeData) {
            is PrimitiveTypeData -> buildPrimitiveSchema(typeData)
            is EnumTypeData -> buildEnumSchema(typeData, context, depth)
            is CollectionTypeData -> buildCollectionSchema(generator, typeData, context, depth)
            is MapTypeData -> buildMapSchema(generator, typeData, context, depth)
            is ObjectTypeData -> buildObjectSchema(generator, typeData, context, depth)
            is WildcardTypeData -> buildAnySchema()
            else -> SwaggerSchema(schema.nullSchema())
        }
    }


    override fun enhance(
        generator: SwaggerSchemaGenerator,
        context: TypeDataContext,
        typeData: BaseTypeData,
        schema: SwaggerSchema,
        depth: Int
    ) = Unit


    private fun buildObjectSchema(
        generator: SwaggerSchemaGenerator,
        typeData: ObjectTypeData,
        context: TypeDataContext,
        depth: Int
    ): SwaggerSchema {
        val definitions = mutableMapOf<String, Schema<*>>()
        val requiredProperties = mutableSetOf<String>()
        val propertySchemas = mutableMapOf<String, Schema<*>>()

        collectMembers(typeData, context).forEach { member ->
            val memberSchema = generator.generate(member.type, context, depth + 1)
            definitions.putAll(memberSchema.definitions)
            propertySchemas[member.name] = memberSchema.schema
            if (!member.nullable) {
                requiredProperties.add(member.name)
            }
        }

        val schema = if (shouldReference(typeData, depth)) {
            definitions[schemaReference(typeData, context)] = schema.objectSchema(propertySchemas, requiredProperties)
            schema.referenceSchema(schemaReference(typeData, context))
        } else {
            schema.objectSchema(propertySchemas, requiredProperties)
        }

        return SwaggerSchema(
            schema = schema,
            definitions = definitions
        )
    }


    private fun collectMembers(typeData: ObjectTypeData, context: TypeDataContext): List<PropertyData> {
        return buildList {
            addAll(typeData.members)
            typeData.supertypes.forEach { supertypeRef ->
                val supertype = supertypeRef.resolve(context)
                if (supertype is ObjectTypeData) {
                    addAll(collectMembers(supertype, context))
                }
            }
        }
    }


    private fun buildCollectionSchema(
        generator: SwaggerSchemaGenerator,
        typeData: CollectionTypeData,
        context: TypeDataContext,
        depth: Int
    ): SwaggerSchema {

        val definitions = mutableMapOf<String, Schema<*>>()

        val itemSchema = generator.generate(typeData.itemType.type, context, depth + 1)
        definitions.putAll(itemSchema.definitions)

        val schema = if (shouldReference(typeData, depth)) {
            definitions[schemaReference(typeData, context)] = schema.arraySchema(itemSchema.schema)
            schema.referenceSchema(schemaReference(typeData, context))
        } else {
            schema.arraySchema(itemSchema.schema)
        }

        return SwaggerSchema(
            schema = schema,
            definitions = definitions
        )
    }


    private fun buildMapSchema(
        generator: SwaggerSchemaGenerator,
        typeData: MapTypeData,
        context: TypeDataContext,
        depth: Int
    ): SwaggerSchema {

        val definitions = mutableMapOf<String, Schema<*>>()

        val valueSchema = generator.generate(typeData.valueType.type, context, depth + 1)
        definitions.putAll(valueSchema.definitions)

        val schema = if (shouldReference(typeData, depth)) {
            definitions[schemaReference(typeData, context)] = schema.mapObjectSchema(valueSchema.schema)
            schema.referenceSchema(schemaReference(typeData, context))
        } else {
            schema.mapObjectSchema(valueSchema.schema)
        }

        return SwaggerSchema(
            schema = schema,
            definitions = definitions
        )
    }


    private fun buildEnumSchema(typeData: EnumTypeData, context: TypeDataContext, depth: Int): SwaggerSchema {
        return if (shouldReference(typeData, depth)) {
            SwaggerSchema(
                schema = schema.referenceSchema(schemaReference(typeData, context)),
                definitions = mutableMapOf(
                    schemaReference(typeData, context) to schema.enumSchema(typeData.enumConstants)
                )
            )
        } else {
            SwaggerSchema(schema.enumSchema(typeData.enumConstants))
        }
    }


    private fun buildWithSubtypes(
        generator: SwaggerSchemaGenerator,
        typeData: ObjectTypeData,
        context: TypeDataContext,
        depth: Int
    ): SwaggerSchema {
        val schemas = typeData.subtypes.map { subtype -> generator.generate(subtype, context, depth + 1) }
        val definitions = mergeDefinitions(schemas.map { it.definitions })
        val schema = if (shouldReference(typeData, depth)) {
            definitions[schemaReference(typeData, context)] = schema.subtypesSchema(schemas.map { it.schema })
            schema.referenceSchema(schemaReference(typeData, context))
        } else {
            schema.subtypesSchema(schemas.map { it.schema })
        }
        return SwaggerSchema(
            schema = schema,
            definitions = definitions
        )
    }


    private fun buildAnySchema(): SwaggerSchema {
        return SwaggerSchema(schema.anyObjectSchema())
    }

    private fun buildPrimitiveSchema(typeData: PrimitiveTypeData): SwaggerSchema {
        return when (typeData.qualifiedName) {
            Number::class.qualifiedName -> schema.numberSchema(false)
            Byte::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(Byte.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(Byte.MAX_VALUE.toLong()),
            )
            Short::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(Short.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(Short.MAX_VALUE.toLong()),
            )
            Int::class.qualifiedName -> schema.int32Schema()
            Long::class.qualifiedName -> schema.int64Schema()
            UByte::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(UByte.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(UByte.MAX_VALUE.toLong()),
            )
            UShort::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(UShort.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(UShort.MAX_VALUE.toLong()),
            )
            UInt::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = BigDecimal.valueOf(UInt.MIN_VALUE.toLong()),
                max = BigDecimal.valueOf(UInt.MAX_VALUE.toLong()),
            )
            ULong::class.qualifiedName -> schema.numberSchema(true)
            Float::class.qualifiedName -> schema.floatSchema()
            Double::class.qualifiedName -> schema.doubleSchema()
            Boolean::class.qualifiedName -> schema.booleanSchema()
            Char::class.qualifiedName -> schema.stringSchema(
                min = 1,
                max = 1,
            )
            String::class.qualifiedName -> schema.stringSchema(
                min = null,
                max = null,
            )
            Any::class.qualifiedName -> schema.anyObjectSchema()
            Unit::class.qualifiedName -> schema.nullSchema()
            else -> schema.nullSchema()
        }.let {
            SwaggerSchema(it)
        }
    }


    private fun shouldReference(type: ObjectTypeData?, depth: Int): Boolean {
        if (depth == 0 && !referenceRoot) {
            return false // don't ref root if not enabled
        }
        return type !is CollectionTypeData && type !is MapTypeData // don't ref arrays and maps, don't ref root
    }

    private fun schemaReference(typeData: BaseTypeData, context: TypeDataContext): String {
        return buildString {
            append(typeData.simpleName)
            if (typeData.typeParameters.isNotEmpty()) {
                append("<")
                typeData.typeParameters.forEach { (key, value) ->
                    value.type.resolve(context)?.also { data ->
                        append(schemaReference(data, context))
                    }
                }
                append(">")
            }
        }
    }

    private fun mergeDefinitions(definitions: List<Map<String, Schema<*>>>): MutableMap<String, Schema<*>> {
        return buildMap {
            definitions.forEach { definitions ->
                definitions.forEach { (defName, defSchema) ->
                    this[defName] = defSchema
                }
            }
        }.toMutableMap()
    }

}