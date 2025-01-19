package io.github.smiley4.schemakenerator.reflection.data

import io.github.smiley4.schemakenerator.core.typedata.TypeData
import kotlin.reflect.KClass
import kotlin.reflect.KType

typealias ReflectionTypeMatcher = (type: KType, clazz: KClass<*>) -> Boolean

typealias ReflectionCustomProcessor = () -> TypeData