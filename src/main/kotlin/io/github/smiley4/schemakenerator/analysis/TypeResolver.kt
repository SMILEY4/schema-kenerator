package io.github.smiley4.schemakenerator.analysis

import io.github.smiley4.schemakenerator.analysis.data.MemberData
import io.github.smiley4.schemakenerator.analysis.data.TypeData
import io.github.smiley4.schemakenerator.analysis.data.TypeParameterData
import io.github.smiley4.schemakenerator.analysis.data.TypeRef
import io.github.smiley4.schemakenerator.getKType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection


class TypeResolver(private val context: TypeContext) {

    companion object {

        // Types that will be analyzed with less detail (no members, supertypes, ...)
        val BASE_TYPES = setOf(
            Number::class,

            Byte::class,
            Short::class,
            Int::class,
            Long::class,

            UByte::class,
            UShort::class,
            UInt::class,
            ULong::class,

            Float::class,
            Double::class,

            Boolean::class,

            Char::class,
            String::class,

            Any::class,
            Unit::class
        )

    }

    inline fun <reified T> resolve(): TypeRef {
        return resolve(getKType<T>())
    }


    fun resolve(type: KType): TypeRef {
        if (type.classifier is KClass<*>) {
            return resolveClass(type, type.classifier as KClass<*>, mapOf())
        } else {
            throw Exception("Type is not a class.")
        }
    }

    private fun resolveClass(type: KType, clazz: KClass<*>, providedTypeParameters: Map<String, TypeParameterData>): TypeRef {

        // resolve all type parameters
        val resolvedTypeParameters = buildMap {
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

        // check if the same type with the same type parameters has already been resolved -> reuse existing
        val ref = TypeRef.forType(type, resolvedTypeParameters)
        if (context.has(ref)) {
            return ref
        }

        // add placeholder to break out of infinite recursions
        context.add(ref, TypeData.placeholder(ref))

        // get all supertypes
        val supertypes =
            if (BASE_TYPES.contains(clazz)) emptyList() else clazz.supertypes.map { resolveSupertype(it, resolvedTypeParameters) }

        // get all members
        val members = if (BASE_TYPES.contains(clazz)) {
            emptyList()
        } else {
            clazz.members
                .filterIsInstance<KProperty<*>>()
                .map { member ->
                    MemberData(
                        name = member.name,
                        type = resolveMemberType(member.returnType, resolvedTypeParameters),
                        nullable = member.returnType.isMarkedNullable
                    )
                }
                .filter { !checkIsSupertypeMember(it, supertypes) }
        }

        // add type to context and return its ref
        return TypeData(
            simpleName = clazz.simpleName!!,
            qualifiedName = clazz.qualifiedName!!,
            typeParameters = resolvedTypeParameters,
            supertypes = supertypes,
            members = members,
        ).let {
            context.add(ref, it)
        }
    }

    private fun resolveTypeProjection(typeProjection: KTypeProjection, providedTypeParameters: Map<String, TypeParameterData>): TypeRef {
        if (typeProjection.type == null) {
            return if (context.has(TypeRef.wildcard())) TypeRef.wildcard() else context.add(TypeRef.wildcard(), TypeData.wildcard())
        }
        return when (val classifier = typeProjection.type?.classifier) {
            is KClass<*> -> {
                resolveClass(typeProjection.type!!, classifier, providedTypeParameters)
            }
            is KTypeParameter -> {
                providedTypeParameters[classifier.name]!!.type
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

    private fun resolveSupertype(type: KType, providedTypeParameters: Map<String, TypeParameterData>): TypeRef {
        return when (val classifier = type.classifier) {
            is KClass<*> -> {
                resolveClass(type, classifier, providedTypeParameters)
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

    private fun resolveMemberType(type: KType, providedTypeParameters: Map<String, TypeParameterData>): TypeRef {
        return when (val classifier = type.classifier) {
            is KClass<*> -> {
                resolveClass(type, classifier, providedTypeParameters)
            }
            is KTypeParameter -> {
                providedTypeParameters[classifier.name]!!.type
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

    private fun checkIsSupertypeMember(member: MemberData, supertypes: List<TypeRef>): Boolean {
        val resolvedSupertypes = supertypes.mapNotNull { context.getData(it) }
        val supertypeMembers = resolvedSupertypes.flatMap { it.members }
        return supertypeMembers.any { it.name == member.name && it.type.id == member.type.id && it.nullable == member.nullable }
    }

}