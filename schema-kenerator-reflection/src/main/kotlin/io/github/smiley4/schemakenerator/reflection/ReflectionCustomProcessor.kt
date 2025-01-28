package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.data.TypeData
import kotlin.reflect.KClass
import kotlin.reflect.KType

typealias ReflectionTypeMatcher = (type: KType, clazz: KClass<*>) -> Boolean

typealias ReflectionCustomProvider = () -> TypeData
