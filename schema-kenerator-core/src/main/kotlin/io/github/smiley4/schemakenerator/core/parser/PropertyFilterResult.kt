package io.github.smiley4.schemakenerator.core.parser

enum class PropertyFilterResult {
    DO_NOT_CARE, KEEP, REMOVE
}

open class PropertyFilter<T> {
    open fun filterProperty(property: T): PropertyFilterResult = PropertyFilterResult.DO_NOT_CARE
    open fun filterProperty(property: PropertyData): PropertyFilterResult = PropertyFilterResult.DO_NOT_CARE
}