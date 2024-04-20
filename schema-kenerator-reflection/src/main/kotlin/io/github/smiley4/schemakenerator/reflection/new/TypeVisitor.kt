package io.github.smiley4.schemakenerator.reflection.new

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter

class TypeVisitor {

    fun process(type: KType, clazz: KClass<*>) {

        // type arguments
        type.arguments.indices.forEach { index ->
            val argType = type.arguments[index]
            when (val classifier = argType.type?.classifier) {
                is KClass<*> -> {
                    process(argType.type!!, classifier)
                }
                is KTypeParameter -> Unit
                else -> Unit
            }
        }


    }



    fun visitType()

}