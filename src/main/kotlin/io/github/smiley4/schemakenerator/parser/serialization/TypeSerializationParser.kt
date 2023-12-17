package io.github.smiley4.schemakenerator.parser.serialization

import io.github.smiley4.schemakenerator.getKType
import io.github.smiley4.schemakenerator.parser.core.TypeParser
import io.github.smiley4.schemakenerator.parser.core.TypeParsingConfig
import io.github.smiley4.schemakenerator.parser.core.TypeParsingContext
import io.github.smiley4.schemakenerator.parser.data.TypeRef
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KType


fun main() {
    TypeSerializationParser(TypeParsingConfig(), TypeParsingContext()).parse<TestClassSimple>()
}


@Serializable
class TestClassSimple {
    val testGen: GenericClass<String>? = GenericClass("h")
}


@Serializable
class GenericClass<T>(val myField: T)


@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class TypeSerializationParser(private val config: TypeParsingConfig, private val context: TypeParsingContext) : TypeParser {

    override fun getContext(): TypeParsingContext = context

    inline fun <reified T> parse(): TypeRef = this.parse(getKType<T>())

    override fun parse(type: KType): TypeRef {
        if (type.classifier is KClass<*>) {
//            return (type.classifier as KClass<*>).serializerOrNull()
//                ?.let { parse(it.descriptor) }
//                ?: TypeRef.wildcard()

            val data = (type.classifier as KClass<*>).serializerOrNull()?.let { parse(it.descriptor) }

            println(data)

            return TypeRef.wildcard()
        } else {
            throw Exception("Type is not a class.")
        }
    }


    private fun parse(descriptor: SerialDescriptor): TypeData {
        return when (descriptor.kind) {
            StructureKind.LIST -> parseList(descriptor)
            StructureKind.MAP -> parseMap(descriptor)
            StructureKind.CLASS -> parseClass(descriptor)
            StructureKind.OBJECT -> parseObject(descriptor)
            PolymorphicKind.OPEN -> parseOpen(descriptor)
            PolymorphicKind.SEALED -> parseSealed(descriptor)
            PrimitiveKind.BOOLEAN -> parsePrimitive(descriptor)
            PrimitiveKind.BYTE -> parsePrimitive(descriptor)
            PrimitiveKind.CHAR -> parsePrimitive(descriptor)
            PrimitiveKind.DOUBLE -> parsePrimitive(descriptor)
            PrimitiveKind.FLOAT -> parsePrimitive(descriptor)
            PrimitiveKind.INT -> parsePrimitive(descriptor)
            PrimitiveKind.LONG -> parsePrimitive(descriptor)
            PrimitiveKind.SHORT -> parsePrimitive(descriptor)
            PrimitiveKind.STRING -> parsePrimitive(descriptor)
            SerialKind.ENUM -> parseEnum(descriptor)
            SerialKind.CONTEXTUAL -> parseClass(descriptor)
        }
    }


    private fun parsePrimitive(descriptor: SerialDescriptor): TypeData {
        return TypeData(
            name = descriptor.serialName,
            kind = descriptor.kind.toString(),
            typeParameters = emptyMap(),
            nullable = descriptor.isNullable
        )
    }

    private fun parseList(descriptor: SerialDescriptor): TypeData {
        val itemType = parse(descriptor.getElementDescriptor(0))
        return TypeData(
            name = descriptor.serialName,
            kind = descriptor.kind.toString(),
            typeParameters = mapOf("item" to itemType),
            nullable = descriptor.isNullable
        )
    }

    private fun parseMap(descriptor: SerialDescriptor): TypeData {
        val keyType = parse(descriptor.getElementDescriptor(0))
        val valueType = parse(descriptor.getElementDescriptor(1))
        return TypeData(
            name = descriptor.serialName,
            kind = descriptor.kind.toString(),
            typeParameters = mapOf("key" to keyType, "value" to valueType),
            nullable = descriptor.isNullable
        )
    }

    private fun parseClass(descriptor: SerialDescriptor): TypeData {
        val members = buildMap {
            for (i in 0..<descriptor.elementsCount) {
                val fieldName = descriptor.getElementName(i)
                val fieldType = parse(descriptor.getElementDescriptor(i))
                this[fieldName] = fieldType
            }
        }
        return TypeData(
            name = descriptor.serialName,
            kind = descriptor.kind.toString(),
            members = members,
            nullable = descriptor.isNullable
        )
    }

    private fun parseOpen(descriptor: SerialDescriptor): TypeData {
        return parseSealed(descriptor)
    }

    private fun parseSealed(descriptor: SerialDescriptor): TypeData {
        val subTypes = descriptor.elementDescriptors.toList()[1].elementDescriptors.map { parse(it) }
        return TypeData(
            name = descriptor.serialName,
            kind = descriptor.kind.toString(),
            subTypes = subTypes,
            nullable = descriptor.isNullable
        )
    }

    private fun parseObject(descriptor: SerialDescriptor): TypeData {
        return TypeData(
            name = descriptor.serialName,
            kind = descriptor.kind.toString(),
            nullable = descriptor.isNullable
        )
    }

    private fun parseEnum(descriptor: SerialDescriptor): TypeData {
        return TypeData(
            name = descriptor.serialName,
            kind = descriptor.kind.toString(),
            enumValues = descriptor.elementNames.toList(),
            nullable = descriptor.isNullable
        )
    }

    private data class TypeData(
        val name: String,
        val kind: String,
        val typeParameters: Map<String, TypeData> = emptyMap(),
        val members: Map<String, TypeData> = emptyMap(),
        val enumValues: List<String> = emptyList(),
        val subTypes: List<TypeData> = emptyList(),
        val nullable: Boolean = false
    )


}