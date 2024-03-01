package io.github.smiley4.schemakenerator.swagger

import io.github.smiley4.schemakenerator.core.parser.BaseTypeData
import io.github.smiley4.schemakenerator.core.parser.CollectionTypeData
import io.github.smiley4.schemakenerator.core.parser.EnumTypeData
import io.github.smiley4.schemakenerator.core.parser.MapTypeData
import io.github.smiley4.schemakenerator.core.parser.ObjectTypeData
import io.github.smiley4.schemakenerator.core.parser.PlaceholderTypeData
import io.github.smiley4.schemakenerator.core.parser.PrimitiveTypeData
import io.github.smiley4.schemakenerator.core.parser.TypeParserContext
import io.github.smiley4.schemakenerator.core.parser.TypeRef
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import io.github.smiley4.schemakenerator.core.parser.id
import io.github.smiley4.schemakenerator.core.parser.resolve
import io.github.smiley4.schemakenerator.core.schema.SchemaGenerator
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.media.Discriminator
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.XML
import java.math.BigDecimal

class SwaggerSchemaGenerator : SchemaGenerator<Schema<*>> {

    override fun generate(typeRef: TypeRef, context: TypeParserContext): Schema<*> {
        val type = typeRef.resolve(context)
        if (type == null) {
            throw IllegalArgumentException("TypeRef could not be resolved")
        }

        return Schema<Any>().also { schema ->

            schema.type = determineType(type)

            if (type is ObjectTypeData) {
                schema.properties = buildMap {
                    type.members.forEach { property ->
                        put(property.name, generate(property.type, context))
                    }
                }
            }

            if(type is CollectionTypeData) {
                schema.items = generate(type.itemType.type, context)
            }


//            schema.contains = Schema<Any>()
//            schema.`$id` = ""
//            schema.types = emptySet()
//            schema.`$schema` = ""
//            schema.`$vocabulary` = ""
//            schema.`$anchor` = ""
//            schema.allOf = emptyList()
//            schema.anyOf = emptyList()
//            schema.oneOf = emptyList()
//            schema.items = Schema<Any>()
//            schema.name = ""
//            schema.discriminator = Discriminator()
//            schema.setDefault("")
//            schema.enum = emptyList()
//            schema.multipleOf = BigDecimal.ZERO
//            schema.maximum = BigDecimal.ZERO
//            schema.exclusiveMaximum = true
//            schema.minimum = BigDecimal.ZERO
//            schema.exclusiveMinimum = true
//            schema.maxLength = 0
//            schema.minLength = 0
//            schema.pattern = ""
//            schema.maxItems = 0
//            schema.minItems = 0
//            schema.uniqueItems = true
//            schema.maxProperties = 0
//            schema.minProperties = 0
//            schema.required = emptyList()
//            schema.not = Schema<Any>()
//            schema.additionalProperties = null
//            schema.description = ""
//            schema.format = ""
//            schema.`$ref` = ""
//            schema.nullable = false
//            schema.readOnly = true
//            schema.writeOnly = false
//            schema.example = null
//            schema.examples = emptyList()
//            schema.exampleSetFlag = false
//            schema.externalDocs = ExternalDocumentation()
//            schema.deprecated = false
//            schema.xml = XML()
//            schema.prefixItems = emptyList()
//            schema.contentEncoding = ""
//            schema.contentMediaType = ""
//            schema.contentSchema = Schema<Any>()
//            schema.propertyNames = Schema<Any>()
//            schema.unevaluatedProperties = Schema<Any>()
//            schema.maxContains = 0
//            schema.minContains = 0
//            schema.additionalItems = Schema<Any>()
//            schema.unevaluatedItems = Schema<Any>()
//            schema.`$comment` = ""
        }
    }

    private fun determineType(type: BaseTypeData): String {
        return when (type) {
            is PrimitiveTypeData -> when (type.qualifiedName) {
                Byte::class.qualifiedName -> "integer"
                Short::class.qualifiedName -> "integer"
                Int::class.qualifiedName -> "integer"
                Long::class.qualifiedName -> "integer"
                UByte::class.qualifiedName -> "integer"
                UShort::class.qualifiedName -> "integer"
                UInt::class.qualifiedName -> "integer"
                ULong::class.qualifiedName -> "integer"
                Float::class.qualifiedName -> "number"
                Double::class.qualifiedName -> "number"
                String::class.qualifiedName -> "string"
                Boolean::class.qualifiedName -> "boolean"
                else -> "object"
            }
            is ObjectTypeData -> when (type) {
                is CollectionTypeData -> "array"
                is EnumTypeData -> "object"
                is MapTypeData -> "object"
                else -> "object"
            }
            is WildcardTypeData -> "any"
            is PlaceholderTypeData -> "null"
        }
    }

}