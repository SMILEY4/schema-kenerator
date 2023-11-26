package io.github.smiley4.schemakenerator

import io.github.smiley4.schemakenerator.data.PropertyInformation
import io.github.smiley4.schemakenerator.data.TypeInformation
import mu.KotlinLogging
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection

class ClassAnalyzer {

    private val logger = KotlinLogging.logger {}

    inline fun <reified T> analyze() = analyze(getKType<T>())

    fun analyze(type: KType, nullable: Boolean = false): TypeInformation {
        return when (val classifier = type.classifier) {
            is KClass<*> -> {
                analyzeClass(type, classifier, nullable)
            }
            else -> {
                logger.warn { "Unhandled classifier type: $classifier" }
                TypeInformation.UNKNOWN
            }
        }
    }

    private fun analyzeClass(type: KType, classifier: KClass<*>, nullable: Boolean = false): TypeInformation {
        return TypeInformation(
            type = type,
            generics = classifier.typeParameters.associateIndexed { i, t -> analyzeGeneric(type.arguments[i], t) },
            simpleName = classifier.simpleName ?: TypeInformation.UNKNOWN.simpleName,
            qualifiedName = classifier.qualifiedName ?: TypeInformation.UNKNOWN.qualifiedName,
            properties = if (includeMembers(classifier)) classifier.members.mapNotNull { analyzeMember(it) } else emptyList(),
            nullable = nullable
        )
    }

    private fun analyzeGeneric(type: KTypeProjection, paramType: KTypeParameter): Pair<String, TypeInformation> {
        return paramType.name to analyze(type.type!!, type.type!!.isMarkedNullable)
    }

    private fun analyzeMember(callable: KCallable<*>): PropertyInformation? {
        return when (callable) {
            is KProperty -> {
                PropertyInformation(
                    name = callable.name,
                    type = callable.returnType,
                    typeInformation = analyze(callable.returnType, callable.returnType.isMarkedNullable)
                )
            }
            is KFunction -> {
                return null
            }
            else -> {
                return null
            }
        }

    }

    private fun includeMembers(owner: KClass<*>): Boolean {
        return owner.qualifiedName?.startsWith("kotlin.")?.let { !it } ?: false
    }

    private fun includeFunction(parent: String?, name: String): Boolean {
        if (parent == null || parent.startsWith("kotlin.")) {
            return false
        }
        if (setOf("equals", "hashCode", "toString").contains(name)) {
            return false
        }
        if (!name.startsWith("get", true) && !name.startsWith("is", true)) {
            return false
        }
        return true
    }

}