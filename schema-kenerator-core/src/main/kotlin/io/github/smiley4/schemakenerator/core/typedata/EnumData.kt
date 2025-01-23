package io.github.smiley4.schemakenerator.core.typedata

/**
 * Additional information about enums
 */
data class EnumData(
    /**
     * the possible values of the enum
     */
    val constants: MutableList<String>,
)
