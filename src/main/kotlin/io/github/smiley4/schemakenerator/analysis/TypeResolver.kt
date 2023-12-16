package io.github.smiley4.schemakenerator.analysis

import io.github.smiley4.schemakenerator.getKType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection


data class TestClassGeneric<T>(
    val value: T
)

data class TestClassDeepGeneric<X>(
    val myValues: Map<String, X>,
    val other: TestClassGeneric<Int>,
    val other2: TestClassGeneric<Int>
)

fun main() {
    val type = getKType<TestClassDeepGeneric<List<Boolean>>>()
    val context = TypeContext()
    val resolved = TypeResolver(context).resolveClass(type, type.classifier as KClass<*>, mapOf())
    println("" + type + resolved + context)
}


data class TypeData(
    val name: String,
    val typeParameters: Map<String, TypeRef>,
    val members: List<MemberData>,
)

data class MemberData(
    val name: String,
    val type: TypeRef
)


class TypeResolver(private val context: TypeContext) {

    fun resolveClass(type: KType, clazz: KClass<*>, resolvedTypeParameters: Map<String, TypeRef>): TypeRef {

        val typeParameters = buildMap {
            for (index in type.arguments.indices) {
                val name = clazz.typeParameters[index].name
                val argType = type.arguments[index]
                this[name] = resolveTypeProjection(argType, resolvedTypeParameters)
            }
        }

        val ref = TypeRef.forType(type, typeParameters)
        if(context.has(ref)) {
            return ref
        }

        val members = clazz.members
            .filterIsInstance<KProperty<*>>()
            .map { member ->
                MemberData(
                    name = member.name,
                    type = resolveMemberType(member.returnType, typeParameters)
                )
            }

        return TypeData(
            name = clazz.simpleName!!,
            typeParameters = typeParameters,
            members = members,
        ).let {
            context.add(type, it)
        }
    }

    private fun resolveTypeProjection(typeProjection: KTypeProjection, resolvedTypeParameters: Map<String, TypeRef>): TypeRef {
        return when (val classifier = typeProjection.type?.classifier) {
            is KClass<*> -> {
                resolveClass(typeProjection.type!!, classifier, resolvedTypeParameters)
            }
            is KTypeParameter -> {
                resolvedTypeParameters[classifier.name]!!
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

    private fun resolveMemberType(type: KType, resolvedTypeParameters: Map<String, TypeRef>): TypeRef {
        return when (val classifier = type.classifier) {
            is KClass<*> -> {
                resolveClass(type, classifier, resolvedTypeParameters)
            }
            is KTypeParameter -> {
                resolvedTypeParameters[classifier.name]!!
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

}