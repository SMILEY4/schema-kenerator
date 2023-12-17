package io.github.smiley4.schemakenerator.parser.reflection

import io.github.smiley4.schemakenerator.parser.core.BASE_TYPES
import io.github.smiley4.schemakenerator.parser.data.TypeParameterData
import io.github.smiley4.schemakenerator.parser.data.TypeRef
import kotlin.reflect.KClass
import kotlin.reflect.KType

class SupertypeParser(private val typeParser: TypeReflectionParser) {

    fun parse(clazz: KClass<*>, resolvedTypeParameters: Map<String, TypeParameterData>): List<TypeRef> {
        return if (BASE_TYPES.contains(clazz)) {
            emptyList()
        } else {
            clazz.supertypes
                .filter { typeParser.getConfig().supertypeFilter.filter(it) }
                .map { resolveSupertype(it, resolvedTypeParameters) }
                .filter { typeParser.getConfig().supertypeFilter.filter(it, typeParser.getContext()) }
        }
    }

    private fun resolveSupertype(type: KType, providedTypeParameters: Map<String, TypeParameterData>): TypeRef {
        return when (val classifier = type.classifier) {
            is KClass<*> -> {
                typeParser.getClassParser().parse(type, classifier, providedTypeParameters)
            }
            else -> {
                throw Exception("Unhandled classifier type")
            }
        }
    }

}