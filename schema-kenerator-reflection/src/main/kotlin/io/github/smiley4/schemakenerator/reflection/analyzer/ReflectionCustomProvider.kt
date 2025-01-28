package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.core.data.TypeData
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Matches a given type to apply a custom provider to
 */
typealias ReflectionTypeMatcher = (type: KType, clazz: KClass<*>) -> Boolean

/**
 * Provide type data for a matched type
 */
typealias ReflectionCustomProvider = () -> TypeData
