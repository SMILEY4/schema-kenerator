package io.github.smiley4.schemakenerator.swagger.module

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeDataContext
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.swagger.SwaggerSchema
import io.github.smiley4.schemakenerator.swagger.SwaggerSchemaGenerator
import io.github.smiley4.schemakenerator.swagger.swagger.SwaggerSchemaUtils
import io.swagger.v3.oas.models.media.Schema

class ReferencingSwaggerSchemaGeneratorModule(val referenceRoot: Boolean = false) : SwaggerSchemaGeneratorModule {

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

        typeData.members.forEach { member ->
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
            Number::class.qualifiedName -> schema.numericSchema(
                integer = false,
                min = null,
                max = null,
            )
            Byte::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = Byte.MIN_VALUE,
                max = Byte.MAX_VALUE,
            )
            Short::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = Short.MIN_VALUE,
                max = Short.MAX_VALUE,
            )
            Int::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = Int.MIN_VALUE,
                max = Int.MAX_VALUE,
            )
            Long::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = Long.MIN_VALUE,
                max = Long.MAX_VALUE,
            )
            UByte::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = UByte.MIN_VALUE.toLong(),
                max = UByte.MAX_VALUE.toLong(),
            )
            UShort::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = UShort.MIN_VALUE.toLong(),
                max = UShort.MAX_VALUE.toLong(),
            )
            UInt::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = UInt.MIN_VALUE.toLong(),
                max = UInt.MAX_VALUE.toLong(),
            )
            ULong::class.qualifiedName -> schema.numericSchema(
                integer = true,
                min = null,
                max = null,
            )
            Float::class.qualifiedName -> schema.numericSchema(
                integer = false,
                min = Float.MIN_VALUE,
                max = Float.MAX_VALUE,
            )
            Double::class.qualifiedName -> schema.numericSchema(
                integer = false,
                min = Double.MIN_VALUE,
                max = Double.MAX_VALUE,
            )
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