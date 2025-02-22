package io.github.smiley4.schemakenerator.serialization.analyzer

import io.github.smiley4.schemakenerator.core.data.WrappedTypeData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor

class SimpleSerializationTypeAnalyzerModule(
    private val matcher: KotlinxSerializationTypeMatcher,
    private val provider: KotlinxSerializationCustomProvider
) : SerializationTypeAnalyzerModule {

    override fun applies(descriptor: SerialDescriptor) = matcher(descriptor)

    @OptIn(ExperimentalSerializationApi::class)
    override fun analyze(context: SerializationTypeAnalyzerModule.Context) = WrappedTypeData(
        typeData = provider(context.id),
        nullable = context.descriptor.isNullable,
    )
}
