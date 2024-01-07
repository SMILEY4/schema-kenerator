package io.github.smiley4.schemakenerator.reflection
import io.github.smiley4.schemakenerator.core.parser.PropertyFilter
import io.github.smiley4.schemakenerator.core.parser.PropertyFilterResult
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility

open class ReflectionPropertyFilter : PropertyFilter<KCallable<*>>()

class VisibilityReflectionPropertyFilter(private val includeHidden: Boolean) : ReflectionPropertyFilter() {
    override fun filterProperty(property: KCallable<*>): PropertyFilterResult {
        return if (property.visibility == KVisibility.PUBLIC) {
            PropertyFilterResult.KEEP
        } else {
            if(includeHidden) PropertyFilterResult.KEEP else PropertyFilterResult.REMOVE
        }
    }
}


class FunctionReflectionPropertyFilter(private val includeFunctions: Boolean) : ReflectionPropertyFilter() {
    override fun filterProperty(property: KCallable<*>): PropertyFilterResult {
        return if(property is KFunction<*>) {
            if(includeFunctions) PropertyFilterResult.KEEP else PropertyFilterResult.REMOVE
        } else {
            PropertyFilterResult.DO_NOT_CARE
        }
    }
}


class WeakGetterReflectionPropertyFilter(private val include: Boolean) : ReflectionPropertyFilter() {

    override fun filterProperty(property: KCallable<*>): PropertyFilterResult {
        if(property is KFunction<*>) {
            if(isGetter(property) && include) {
                return PropertyFilterResult.KEEP
            } else {
                return PropertyFilterResult.REMOVE
            }
        } else {
            return PropertyFilterResult.DO_NOT_CARE
        }
    }

    private fun isGetter(property: KFunction<*>): Boolean {
        return property.returnType !== Unit::class
                && property.parameters.none { it.name != null }
    }
}


class TrueGetterReflectionPropertyFilter(private val include: Boolean) : ReflectionPropertyFilter() {

    override fun filterProperty(property: KCallable<*>): PropertyFilterResult {
        if(property is KFunction<*>) {
            if(isGetter(property) && include) {
                return PropertyFilterResult.KEEP
            } else {
                return PropertyFilterResult.REMOVE
            }
        } else {
            return PropertyFilterResult.DO_NOT_CARE
        }
    }

    private fun isGetter(property: KCallable<*>): Boolean {
        return property is KFunction<*>
                && property.returnType !== Unit::class
                && property.name.let { it.startsWith("get") || it.startsWith("is") }
    }
}