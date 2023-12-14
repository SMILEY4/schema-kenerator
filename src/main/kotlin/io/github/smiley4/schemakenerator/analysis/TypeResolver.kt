package io.github.smiley4.schemakenerator.analysis

import io.github.smiley4.schemakenerator.getKType
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection


data class TestClassGeneric<T>(
    val value: T
)

data class TestClassDeepGeneric<X>(
    val myValues: Map<String, X>,
    val other: TestClassGeneric<Int>
)


fun main() {

    val type = getKType<TestClassDeepGeneric<String>>()

    TypeResolver().resolve(type.classifier as KClass<*>, type.arguments)

}

data class ClassData(
    val generics: List<GenericData>,
    val members: List<MemberData>
)

data class GenericData(
    val name: String,
    val type: KType
)

data class MemberData(
    val name: String,
    val gType: GenericData?,
    val cType: KClass<*>?,
)

class TypeResolver {

    fun resolve(clazz: KClass<*>, typeParams: List<KTypeProjection>): ClassData {

        val genericData = typeParams.mapIndexed { index, typeParam -> GenericData(clazz.typeParameters[index].name, typeParam.type!!) }
        val genericDataMap = genericData.associateBy { it.name }

        val members = clazz.members.map { member ->
            resolveMember(member, genericDataMap)

//            val memberName = member.name
//            val memberTypeClassifier = member.returnType.classifier!!
//            if(memberTypeClassifier is KTypeParameter) {
//                val genericType = genericDataMap[memberTypeClassifier.name]
//                MemberData(memberName, genericType, null)
//            } else {
//                val memberTypeParams = member.returnType.arguments
//                // ...resolve member.returnType.classifier + resolved memberTypeParams
//                MemberData(memberName, null, memberTypeClassifier as KClass<*>)
//            }
        }

        return ClassData(
            generics = genericData,
            members = members
        )
    }


    fun resolveTypeParameter(type: KType) {

    }



    private fun resolveMember(member: KCallable<*>, generics: Map<String, GenericData>): MemberData {
        val type = member.returnType
        val classifier = member.returnType.classifier!!

        return when (classifier) {
//            // member is directly of generic type specified by class
//            is KTypeParameter -> {
//                MemberData(
//                    member.name,
//                    gType = generics[classifier.name],
//                    cType = null,
//                    0
//                )
//            }
//            // member is another type, possibly with nested generic type specified by class
//            is KClass<*> -> {
//                MemberData(
//                    member.name,
//                    gType = null,
//                    cType = classifier
//                )
//            }
            // anything else
            else -> {
                MemberData(
                    member.name,
                    gType = null,
                    cType = null,
                )
            }
        }
    }


}