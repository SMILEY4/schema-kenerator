package io.github.smiley4.schemakenerator.reflection

import io.github.smiley4.schemakenerator.core.parser.PropertyFilter
import io.github.smiley4.schemakenerator.core.parser.PropertyFilterResult
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.valueParameters

open class ReflectionPropertyFilter : PropertyFilter<KCallable<*>>()


class FunctionReflectionPropertyFilter : ReflectionPropertyFilter() {

    override fun filterProperty(property: KCallable<*>): PropertyFilterResult {
        return if (isOtherFunction(property)) {
            PropertyFilterResult.REMOVE
        } else {
            PropertyFilterResult.DO_NOT_CARE
        }
    }

    private fun isOtherFunction(property: KCallable<*>): Boolean {
        return property is KFunction<*> && (property.returnType == Unit::class || property.valueParameters.isNotEmpty())
    }

}


class GetterReflectionPropertyFilter(private val include: Boolean) : ReflectionPropertyFilter() {

    override fun filterProperty(property: KCallable<*>): PropertyFilterResult {
        if (isGetter(property)) {
            return if (include) {
                PropertyFilterResult.KEEP
            } else {
                PropertyFilterResult.REMOVE
            }
        }
        return PropertyFilterResult.DO_NOT_CARE
    }

    private fun isGetter(property: KCallable<*>): Boolean {
        return property is KFunction<*>
                && property.returnType !== Unit::class
                && property.valueParameters.isEmpty()
                && property.name.let { it.startsWith("get") || it.startsWith("is") }
    }

}

class WeakGetterReflectionPropertyFilter(private val include: Boolean) : ReflectionPropertyFilter() {

    override fun filterProperty(property: KCallable<*>): PropertyFilterResult {
        if (isWeakGetter(property)) {
            return if (include) {
                PropertyFilterResult.KEEP
            } else {
                PropertyFilterResult.REMOVE
            }
        }
        return PropertyFilterResult.DO_NOT_CARE
    }

    private fun isWeakGetter(property: KCallable<*>): Boolean {
        return property is KFunction<*>
                && property.returnType !== Unit::class
                && property.valueParameters.isEmpty()
                && !property.name.let { it.startsWith("get") || it.startsWith("is") }
    }

}

class VisibilityGetterReflectionPropertyFilter(private val includeHidden: Boolean) : ReflectionPropertyFilter() {

    override fun filterProperty(property: KCallable<*>): PropertyFilterResult {
        return if (property.visibility == KVisibility.PUBLIC) {
            PropertyFilterResult.KEEP
        } else {
            if (includeHidden) {
                PropertyFilterResult.KEEP
            } else {
                PropertyFilterResult.REMOVE
            }
        }
    }

}
