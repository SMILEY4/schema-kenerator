package io.github.smiley4.schemakenerator.serialization.steps

import io.github.smiley4.schemakenerator.core.typedata.WrappedTypeData
import io.github.smiley4.schemakenerator.serialization.analyzer.SerializationTypeAnalyzerModule
import kotlinx.serialization.descriptors.SerialDescriptor

class SimpleSerializationTypeAnalyzerModule(
    private val matcher: KotlinxSerializationTypeMatcher,
    private val provider: KotlinxSerializationCustomProvider
) : SerializationTypeAnalyzerModule {

    override fun applies(descriptor: SerialDescriptor) = matcher(descriptor)

    override fun analyze(context: SerializationTypeAnalyzerModule.Context) = WrappedTypeData(
        typeData = provider(),
        nullable = context.nullable,
    )
}
