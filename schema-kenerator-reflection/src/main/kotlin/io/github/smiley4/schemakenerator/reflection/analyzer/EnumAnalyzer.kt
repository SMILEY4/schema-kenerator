package io.github.smiley4.schemakenerator.reflection.analyzer

import io.github.smiley4.schemakenerator.reflection.data.EnumConstType
import kotlin.reflect.KClass

/**
 * Analysis functions for enums using reflection.
 */
class EnumAnalyzer {

    /**
     * Analyzes the given enum class.
     * @param clazz the enum class to analyze the constants of
     * @param enumConstType the format/[EnumConstType] for the results
     * @return the enum constants in a format specified in [enumConstType] (see [EnumConstType])
     */
    fun analyzeEnumConstants(clazz: KClass<*>, enumConstType: EnumConstType): List<String> {
        return clazz.java.enumConstants
            ?.map {
                when (enumConstType) {
                    EnumConstType.NAME -> (it as Enum<*>).name
                    EnumConstType.TO_STRING -> it.toString()
                }
            }
            ?: emptyList()
    }

}
