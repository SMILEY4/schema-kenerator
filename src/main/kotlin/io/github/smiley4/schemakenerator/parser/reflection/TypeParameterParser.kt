package io.github.smiley4.schemakenerator.parser.reflection

import io.github.smiley4.schemakenerator.parser.data.TypeData
import io.github.smiley4.schemakenerator.parser.data.TypeParameterData
import io.github.smiley4.schemakenerator.parser.data.TypeRef
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection

class TypeParameterParser(private val typeParser: TypeReflectionParser) {

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
            return if (typeParser.getContext().has(TypeRef.wildcard())) TypeRef.wildcard() else typeParser.getContext().add(TypeRef.wildcard(), TypeData.wildcard())
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

}