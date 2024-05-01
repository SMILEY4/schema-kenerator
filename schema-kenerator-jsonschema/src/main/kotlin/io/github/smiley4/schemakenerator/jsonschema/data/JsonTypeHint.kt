package io.github.smiley4.schemakenerator.jsonschema.data

@Target(
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonTypeHint(val type: String)
