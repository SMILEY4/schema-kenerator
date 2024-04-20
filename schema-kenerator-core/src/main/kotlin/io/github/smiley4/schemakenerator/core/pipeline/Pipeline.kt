package io.github.smiley4.schemakenerator.core.pipeline

import kotlin.reflect.KType

//abstract class Pipeline<IN, OUT> {
//    var next: Pipeline<OUT, *>? = null
//    protected abstract fun step(value: IN): Collection<OUT>
//}
//
//
//fun pipeline() = StartPipeline()
//
//class StartPipeline : Pipeline<Unit, Unit>() {
//    override fun step(value: Unit): Collection<Unit> = emptyList()
//}
//
//
//
//fun <T> Pipeline<*, Unit>.type() = FillTypePipeline(listOf()).also { this.next = it }
//
//class FillTypePipeline(private val types: List<KType>) : Pipeline<Unit, KType>() {
//    override fun step(value: Unit): Collection<KType> = types
//}
//
//
//
//fun Pipeline<*, KType>.findJacksonSubTypes() = FindJacksonSubTypesPipeline().also { this.next = it }
//
//class FindJacksonSubTypesPipeline : Pipeline<KType, KType>() {
//    override fun step(value: KType): Collection<KType> = emptyList()
//}
//
//
//
//fun <T> Pipeline<*, T>.collect() = emptyList<T>().also { this.next = null }


fun main() {

//    pipeline()
//        .type<String>()
//        .findJacksonSubTypes()
//        .collect()

}