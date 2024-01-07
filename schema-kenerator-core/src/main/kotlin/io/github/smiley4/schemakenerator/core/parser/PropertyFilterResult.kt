package io.github.smiley4.schemakenerator.core.parser

enum class PropertyFilterResult {
    DO_NOT_CARE, REMOVE, KEEP
}


/**
 * decide which properties/members to keep and which to drop
 * INCLUDE if ...
 *  ... no filter results in [PropertyFilterResult.REMOVE] ...
 *  ... AND at least one filter results in [PropertyFilterResult.KEEP]
 * EXCLUDE if ...
 *  ... at least one filter results in [PropertyFilterResult.REMOVE] ...
 *  ... OR no filter results in [PropertyFilterResult.KEEP]
 */
open class PropertyFilter<T> {

    open fun filterProperty(property: T): PropertyFilterResult = PropertyFilterResult.KEEP
    open fun filterProperty(property: PropertyData): PropertyFilterResult = PropertyFilterResult.KEEP

    companion object {

        fun <T> applyFilters(property: T, filters: Collection<PropertyFilter<T>>): Boolean {
            println()
            println(property)
            val results = filters.map { it.filterProperty(property) }.toSet()
            println()
            return decideFilterResult(results)
        }

        fun <T> applyFilters(property: PropertyData, filters: Collection<PropertyFilter<T>>): Boolean {
            val results = filters.map { it.filterProperty(property) }.toSet()
            return decideFilterResult(results)
        }

        private fun decideFilterResult(results: Collection<PropertyFilterResult>): Boolean {
            if (results.contains(PropertyFilterResult.REMOVE)) {
                return false
            }
            if (results.contains(PropertyFilterResult.KEEP)) {
                return true
            }
            return false
        }

    }

}