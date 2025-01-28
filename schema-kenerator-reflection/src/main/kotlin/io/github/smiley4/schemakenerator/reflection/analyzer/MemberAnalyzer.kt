package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.core.data.MemberData
import io.github.smiley4.schemakenerator.core.data.MemberKind
import io.github.smiley4.schemakenerator.core.data.TypeData
import io.github.smiley4.schemakenerator.core.data.Visibility
import io.github.smiley4.schemakenerator.core.data.WrappedTypeId
import io.github.smiley4.schemakenerator.core.data.findOrThrow
import io.github.smiley4.schemakenerator.core.data.toWrappedTypeId
import java.lang.reflect.Modifier
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

/**
 * Analysis functions for members using reflection.
 */
class MemberAnalyzer {

    /**
     * Analyzes members of the current class of the given context.
     * @param context the context with the current type to analyze
     * @param includeGetters whether to include getters as members of classes (see [MemberKind.GETTER]).
     * @param includeWeakGetters whether to include weak getters as members of classes (see [MemberKind.WEAK_GETTER]).
     * @param includeFunctions whether to include functions as members of classes (see [MemberKind.FUNCTION]).
     * @param includeHidden whether to include hidden (e.g. private) members
     * @param includeStatic whether to include static members
     * @param annotationAnalyzer function providing the annotation data for the given member
     * @return the list of members
     */
    @Suppress("LongParameterList")
    fun analyzeMembers(
        context: ReflectionTypeAnalyzerModule.Context,
        includeGetters: Boolean,
        includeWeakGetters: Boolean,
        includeFunctions: Boolean,
        includeHidden: Boolean,
        includeStatic: Boolean,
        annotationAnalyzer: (member: KCallable<*>, ctx: ReflectionTypeAnalyzerModule.Context) -> List<AnnotationData>
    ): List<MemberData> {
        return getMembersSafe(context.clazz)
            .asSequence()
            .filter { shouldIncludeMember(it, includeGetters, includeWeakGetters, includeFunctions, includeHidden, includeStatic) }
            .mapNotNull { analyzeMember(it, context, annotationAnalyzer) }
            .distinctBy { it.name }
            .toList()
    }


    /**
     * Checks whether the given member should be included in the analysis result.
     * @param includeGetters whether to include getters as members of classes (see [MemberKind.GETTER]).
     * @param includeWeakGetters whether to include weak getters as members of classes (see [MemberKind.WEAK_GETTER]).
     * @param includeFunctions whether to include functions as members of classes (see [MemberKind.FUNCTION]).
     * @param includeHidden whether to include hidden (e.g. private) members
     * @param includeStatic whether to include static members
     * @return whether the given type member should be included in the resulting type data
     */
    @Suppress("LongParameterList")
    fun shouldIncludeMember(
        member: KCallable<*>,
        includeGetters: Boolean,
        includeWeakGetters: Boolean,
        includeFunctions: Boolean,
        includeHidden: Boolean,
        includeStatic: Boolean,
    ): Boolean {
        // check static
        if (!includeStatic) {
            when (member) {
                is KProperty<*> -> if (canAccessJavaField(member) && Modifier.isStatic(member.javaField?.modifiers ?: 0)) {
                    return false
                }
                is KFunction<*> -> if (canAccessJavaMethod(member) && Modifier.isStatic(member.javaMethod?.modifiers ?: 0)) {
                    return false
                }
            }
        }
        // check visibility
        val visibility = determinePropertyVisibility(member)
        when (visibility) {
            Visibility.PUBLIC -> Unit
            Visibility.HIDDEN -> if (!includeHidden) return false
        }
        // check function type
        return if (member is KFunction<*>) {
            when (determineFunctionMemberKind(member)) {
                MemberKind.GETTER -> includeGetters
                MemberKind.WEAK_GETTER -> includeWeakGetters
                MemberKind.FUNCTION -> includeFunctions
                else -> true
            }
        } else {
            true
        }
    }


    /**
     * @return the [Visibility] of the given member
     */
    fun determinePropertyVisibility(member: KCallable<*>): Visibility {
        return if (member.visibility == KVisibility.PUBLIC) Visibility.PUBLIC else Visibility.HIDDEN
    }


    /**
     * @return the specific [MemberKind] of the given function, i.e. whether it is a "normal" function, a getter or a "weak" getter
     */
    fun determineFunctionMemberKind(function: KFunction<*>): MemberKind {
        if (function.returnType == Unit::class || function.parameters.any { it.name != null }) {
            return MemberKind.FUNCTION
        }
        if (function.name.startsWith("get") || function.name.startsWith("is")) {
            return MemberKind.GETTER
        }
        return MemberKind.WEAK_GETTER
    }


    /**
     * Analyzes the given member.
     * @param member the member to analyze
     * @param context the context with the current type and data to analyze
     * @param annotationAnalyzer function providing the annotation data for the given member
     * @return the resulting data
     */
    fun analyzeMember(
        member: KCallable<*>,
        context: ReflectionTypeAnalyzerModule.Context,
        annotationAnalyzer: (member: KCallable<*>, ctx: ReflectionTypeAnalyzerModule.Context) -> List<AnnotationData>
    ): MemberData? {
        return when (member) {
            is KProperty<*> -> analyzeProperty(member, context, annotationAnalyzer)
            is KFunction<*> -> analyzeFunction(member, context, annotationAnalyzer)
            else -> null
        }
    }


    /**
     * Analyzes the given member property
     * @param member the property
     * @param context the context with the current type and data to analyze
     * @param annotationAnalyzer function providing the annotation data for the given member
     * @return the resulting data
     */
    fun analyzeProperty(
        member: KProperty<*>,
        context: ReflectionTypeAnalyzerModule.Context,
        annotationAnalyzer: (member: KCallable<*>, ctx: ReflectionTypeAnalyzerModule.Context) -> List<AnnotationData>
    ): MemberData {

        val isOptional = context.clazz.constructors.any { constructor ->
            val ctorParameter = constructor.parameters.find { parameter ->
                parameter.name == member.name && parameter.type == member.returnType
            }
            ctorParameter?.isOptional ?: false
        }
        val type = resolveMemberType(member.returnType, context)
        return MemberData(
            name = member.name,
            type = type.id,
            nullable = member.returnType.isMarkedNullable || type.nullable,
            optional = isOptional,
            annotations = annotationAnalyzer(member, context).toMutableList(),
            kind = MemberKind.PROPERTY,
            visibility = determinePropertyVisibility(member)
        )
    }


    /**
     * Analyzes the given member function
     * @param member the function
     * @param context the context with the current type and data to analyze
     * @param annotationAnalyzer function providing the annotation data for the given member
     * @return the resulting data
     */
    fun analyzeFunction(
        member: KFunction<*>,
        context: ReflectionTypeAnalyzerModule.Context,
        annotationAnalyzer: (member: KCallable<*>, ctx: ReflectionTypeAnalyzerModule.Context) -> List<AnnotationData>
    ): MemberData {
        val type = resolveMemberType(member.returnType, context)
        return MemberData(
            name = member.name,
            type = type.id,
            nullable = member.returnType.isMarkedNullable || type.nullable,
            optional = false,
            annotations = annotationAnalyzer(member, context).toMutableList(),
            kind = determineFunctionMemberKind(member),
            visibility = determinePropertyVisibility(member),
        )
    }


    /**
     * Resolves the type of the given member (as wrapped [TypeData])
     * @param type the type of the member
     * @param context the context with the current type and data to analyze
     * @return the resolved [TypeData] with nullability information
     */
    fun resolveMemberType(type: KType, context: ReflectionTypeAnalyzerModule.Context): WrappedTypeId {
        return when (val classifier = type.classifier) {
            is KClass<*> -> context.analyze(type, classifier).toWrappedTypeId()
            is KTypeParameter -> {
                val typeParameter = context.knownTypeParameters.findOrThrow(classifier.name)
                WrappedTypeId(
                    id = context.knownTypeData.findOrThrow(typeParameter.type).id,
                    nullable = typeParameter.nullable
                )
            }
            else -> throw IllegalArgumentException("Unhandled classifier type")
        }
    }


    /**
     * @return members of the class (or an empty list on unexpected error).
     */
    @Suppress("SwallowedException")
    fun getMembersSafe(clazz: KClass<*>): Collection<KCallable<*>> {
        /*

        Throws error for function-types (for unknown reasons). Catch and ignore this error

        Example:
        class MyClass(
            val myField: (v: Int) -> String
        )

        "Unknown origin of public abstract operator fun invoke(p1: P1):
        R defined in kotlin.Function1[FunctionInvokeDescriptor@162989f2]
        (class kotlin.reflect.jvm.internal.impl.builtins.functions.FunctionInvokeDescriptor)"

        */
        return try {
            clazz.members
        } catch (e: Throwable) {
            emptyList()
        }
    }


    /**
     * Safely checks if we can even access the underlying java field.
     */
    @Suppress("SwallowedException")
    private fun canAccessJavaField(property: KProperty<*>): Boolean {
        return try {
            property.javaField != null
        } catch (e: Throwable) {
            false
        }
    }


    /**
     * Safely checks if we can even access the underlying java method.
     */
    @Suppress("SwallowedException")
    private fun canAccessJavaMethod(property: KFunction<*>): Boolean {
        return try {
            property.javaMethod != null
        } catch (e: Throwable) {
            false
        }
    }

}
