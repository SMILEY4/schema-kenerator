package io.github.smiley4.schemakenerator.core

import io.github.smiley4.schemakenerator.core.data.Bundle

/**
 * A generic step that takes an object of type [IN] as input and returns an object of type [OUT]
 */
interface GenericStep<IN, OUT> {
    fun process(input: IN): OUT
}

/**
 * A generic step that takes a bundle with content of type [IN] as input and returns a bundle with content of type [OUT]
 */
interface GenericBundleStep<IN, OUT> : GenericStep<Bundle<IN>, Bundle<OUT>>


/**
 * A generic step that takes a bundle with content of type [T] and calls the "process"-function for each entry in the bundle.
 */
abstract class GenericBundleIndependentContentStep<T> : GenericStep<Bundle<T>, Bundle<T>> {

    override fun process(input: Bundle<T>): Bundle<T> {
        return input.also { data ->
            process(data.data)
            data.supporting.forEach { process(it) }
        }
    }

    protected abstract fun process(input: T)

}
