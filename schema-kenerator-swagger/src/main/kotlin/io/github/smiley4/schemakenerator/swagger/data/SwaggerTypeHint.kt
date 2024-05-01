package io.github.smiley4.schemakenerator.swagger.data

@Target(
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class SwaggerTypeHint(val type: String)
