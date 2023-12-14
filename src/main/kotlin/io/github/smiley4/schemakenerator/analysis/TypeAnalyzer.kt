package io.github.smiley4.schemakenerator.analysis

import io.github.smiley4.schemakenerator.newdata.AnalysisContext
import io.github.smiley4.schemakenerator.newdata.GenericInformation
import io.github.smiley4.schemakenerator.newdata.PropertyInformation
import io.github.smiley4.schemakenerator.newdata.TypeInformation
import io.github.smiley4.schemakenerator.newdata.TypeRef
import mu.KotlinLogging
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection

class TypeAnalyzer {

    private val logger = KotlinLogging.logger {}


    fun analyze(context: AnalysisContext, type: KType, availableGenerics: List<GenericInformation>): TypeRef {
        return when (val classifier = type.classifier) {
            is KClass<*> -> analyzeClass(context, type, classifier)
            is KTypeParameter -> availableGenerics.find { it.localName == classifier.name }?.typeRef ?: TypeRef.INVALID
            else -> {
                logger.warn { "Unhandled classifier type: $classifier" }
                TypeRef.INVALID
            }
        }
    }

    //====================//
    //        CLASS       //
    //====================//

    private fun analyzeClass(context: AnalysisContext, type: KType, clazz: KClass<*>): TypeRef {
        val typeRef = context.getTypeRef(type)
        return if (typeRef != null)
            typeRef
        else {
            val generics = getGenerics(context, type, clazz)
            context.addType(
                type, TypeInformation(
                    simpleName = getSimpleName(clazz),
                    qualifiedName = getQualifiedName(clazz),
                    generics = generics,
                    properties = getProperties(context, clazz, generics),
                    superTypes = getSuperTypes(),
                    baseType = getIsBaseType()
                )
            )
        }
    }

    private fun getSimpleName(clazz: KClass<*>): String {
        return clazz.simpleName ?: TypeInformation.UNKNOWN.simpleName
    }

    private fun getQualifiedName(clazz: KClass<*>): String {
        return clazz.qualifiedName ?: TypeInformation.UNKNOWN.qualifiedName
    }

    private fun getGenerics(context: AnalysisContext, type: KType, clazz: KClass<*>): List<GenericInformation> {
        return type.arguments.mapIndexed { index, arg -> analyzeGeneric(context, clazz.typeParameters[index], arg) }
    }

    private fun getProperties(
        context: AnalysisContext,
        clazz: KClass<*>,
        availableGenerics: List<GenericInformation>
    ): List<PropertyInformation> {
        return clazz.members.mapNotNull { analyzeMember(context, it, availableGenerics) }
    }

    private fun getSuperTypes(): List<TypeRef> {
        return emptyList() // todo
    }

    private fun getIsBaseType(): Boolean {
        return false // todo
    }

    //====================//
    //       GENERIC      //
    //====================//

    private fun analyzeGeneric(context: AnalysisContext, typeParam: KTypeParameter, typeProjection: KTypeProjection): GenericInformation {
        return GenericInformation(
            name = typeParam.name,
            localName = getName(typeProjection),
            typeRef = typeProjection.type?.let { analyze(context, it, emptyList()) } ?: TypeRef.INVALID,
            nullable = typeProjection.type?.isMarkedNullable ?: false
        )
    }

    private fun getName(typeProjection: KTypeProjection): String {
        return when (val classifier = typeProjection.type?.classifier) {
            is KTypeParameter -> classifier.name
            else -> "?"
        }
    }

    //====================//
    //       MEMBER       //
    //====================//

    private fun analyzeMember(
        context: AnalysisContext,
        callable: KCallable<*>,
        availableGenerics: List<GenericInformation>
    ): PropertyInformation? {
        return when (callable) {
            is KProperty -> {
                PropertyInformation(
                    name = callable.name,
                    typeRef = analyze(context, callable.returnType, availableGenerics),
                    nullable = callable.returnType.isMarkedNullable
                )
            }
            else -> {
                null
            }
        }
    }

}