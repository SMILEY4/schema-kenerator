package io.github.smiley4.schemakenerator.reflection.parsers

import io.github.smiley4.schemakenerator.core.parser.ContextTypeRef
import io.github.smiley4.schemakenerator.core.parser.InlineTypeRef
import io.github.smiley4.schemakenerator.core.parser.TypeId
import io.github.smiley4.schemakenerator.core.parser.TypeParameterData
import io.github.smiley4.schemakenerator.core.parser.TypeRef
import io.github.smiley4.schemakenerator.core.parser.WildcardTypeData
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection

class TypeParameterParser(private val typeParser: ReflectionTypeParser) {

    fun parse(type: KType, clazz: KClass<*>, providedTypeParameters: Map<String, TypeParameterData>): Map<String, TypeParameterData> {
        return buildMap {
            for (index in type.arguments.indices) {
                val name = clazz.typeParameters[index].name
                val argType = type.arguments[index]
                val resolvedType = resolveTypeProjection(argType, providedTypeParameters)
                this[name] = TypeParameterData(
                    name = name,
                    type = resolvedType,
                    nullable = argType.type?.isMarkedNullable ?: false
                )
            }
        }
    }

    private fun resolveTypeProjection(typeProjection: KTypeProjection, providedTypeParameters: Map<String, TypeParameterData>): TypeRef {
        if (typeProjection.type == null) {
            return resolveWildcard()
        }
        return when (val classifier = typeProjection.type?.classifier) {
            is KClass<*> -> {
                typeParser.getClassParser().parse(typeProjection.type!!, classifier, providedTypeParameters)
            }
            is KTypeParameter -> {
                providedTypeParameters[classifier.name]!!.type
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

    private fun resolveWildcard(): TypeRef {
        return if (typeParser.config.inline) {
            InlineTypeRef(WildcardTypeData())
        } else {
            if (typeParser.context.has(TypeId.wildcard())) {
                ContextTypeRef(TypeId.wildcard())
            } else {
                typeParser.context.add(WildcardTypeData())
            }
        }
    }

}