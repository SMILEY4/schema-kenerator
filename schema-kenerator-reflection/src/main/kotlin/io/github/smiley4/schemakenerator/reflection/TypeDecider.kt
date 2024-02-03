package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.TypeId
import kotlin.reflect.KClass

interface TypeDecider {

    fun determineType(config: ReflectionTypeParserConfig, clazz: KClass<*>, id: TypeId): ClassType

}


class TypeDeciderImpl : TypeDecider {

    override fun determineType(config: ReflectionTypeParserConfig, clazz: KClass<*>, id: TypeId): ClassType {
        return when {
            config.primitiveTypes.contains(clazz) -> ClassType.PRIMITIVE
            isEnum(clazz) -> ClassType.ENUM
            isCollection(clazz) -> ClassType.COLLECTION
            isMap(clazz) -> ClassType.MAP
            else -> ClassType.OBJECT
        }
    }


    private fun isEnum(clazz: KClass<*>): Boolean {
        return clazz.java.enumConstants !== null
    }

    private fun isCollection(clazz: KClass<*>): Boolean {
        return if (clazz.qualifiedName == Collection::class.qualifiedName || clazz.qualifiedName == Array::class.qualifiedName) {
            true
        } else {
            clazz.supertypes
                .asSequence()
                .mapNotNull { it.classifier }
                .map { it as KClass<*> }
                .any { isCollection(it) }
        }
    }

    private fun isMap(clazz: KClass<*>): Boolean {
        return if (clazz.qualifiedName == Map::class.qualifiedName) {
            true
        } else {
            clazz.supertypes
                .asSequence()
                .mapNotNull { it.classifier }
                .map { it as KClass<*> }
                .any { isMap(it) }
        }
    }

}