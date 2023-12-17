package io.github.smiley4.schemakenerator.parser.reflection

import io.github.smiley4.schemakenerator.parser.data.TypeData
import io.github.smiley4.schemakenerator.parser.data.TypeParameterData
import io.github.smiley4.schemakenerator.parser.data.TypeRef
import kotlin.reflect.KClass
import kotlin.reflect.KType

class ClassParser(private val typeParser: TypeReflectionParser) {

    fun parse(type: KType, clazz: KClass<*>, providedTypeParameters: Map<String, TypeParameterData>): TypeRef {

        // resolve all type parameters
        val resolvedTypeParameters = typeParser.getTypeParameterParser().parse(type, clazz, providedTypeParameters)

        // check if the same type with the same type parameters has already been resolved -> reuse existing
        val ref = TypeRef.forType(type, resolvedTypeParameters)
        if (typeParser.getContext().has(ref)) {
            return ref
        }

        // add placeholder to break out of infinite recursions
        typeParser.getContext().reserve(ref)

        // collect more information
        val supertypes = typeParser.getSupertypeParser().parse(clazz, resolvedTypeParameters)
        val members = typeParser.getMemberParser().parse(clazz, resolvedTypeParameters, supertypes)
        val enumValues = typeParser.getEnumValueParser().parse(clazz)

        // add type to context and return its ref
        return TypeData(
            simpleName = clazz.simpleName!!,
            qualifiedName = clazz.qualifiedName!!,
            typeParameters = resolvedTypeParameters,
            supertypes = supertypes,
            members = members,
            enumValues = enumValues
        ).let {
            typeParser.getContext().add(ref, it)
        }
    }

}