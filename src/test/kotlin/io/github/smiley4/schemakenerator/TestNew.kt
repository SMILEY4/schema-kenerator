package io.github.smiley4.schemakenerator

import io.github.smiley4.schemakenerator.analysis.TypeAnalyzer
import io.github.smiley4.schemakenerator.models.TestClassDeepGeneric
import io.github.smiley4.schemakenerator.models.TestClassGeneric
import io.github.smiley4.schemakenerator.newdata.AnalysisContext
import io.kotest.core.spec.style.StringSpec

class TestNew : StringSpec({

    "test" {

        val context = AnalysisContext()
        val type = getKType<TestClassGeneric<TestClassDeepGeneric<String>>>()

        val ref = TypeAnalyzer().analyze(context, type, emptyList())

        println(ref)

    }

})