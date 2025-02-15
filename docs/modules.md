# Modules

The schema-kenerator project is split into multiple artifacts, each providing a set of steps and functionality.
The required artifacts depend on the use case, e.g. whether a swagger schema or json schema should be generated or 
whether Jackson annotations need to be included.

## Core

<div class="grid cards" markdown>

-   __Core__

    ---

    `schema-kenerator-core`

    Contains base data classes, common steps and annotations to use with all type analysis and schema generation steps.

    [:octicons-arrow-right-24: API Reference](dokka/schema-kenerator-core/index.html)

</div>

## Type Analysis

<div class="grid cards" markdown>

-   __Reflection Type Analysis__

    ---

    `schema-kenerator-reflection`

    Analyze types and extract information using reflection. Includes additional steps and annotations to modify and enrich extracted data.

    [:octicons-arrow-right-24: API Reference](dokka/schema-kenerator-reflection/index.html)


-   __Kotlinx.Serialization__

    ---

    `schema-kenerator-serialization`

    Analyze types and extract information using Kotlinx.Serialization. Includes additional steps and annotations to modify and enrich extracted data.

    [:octicons-arrow-right-24: API Reference](dokka/schema-kenerator-serialization/index.html)

</div>


## Schema Generation

<div class="grid cards" markdown>

-   __JSON Schema Generation__

    ---

    `schema-kenerator-jsonschema`

    Generate JSON schemas from analyzed types and further customize and enrich schemas with additional steps and annotations.

    [:octicons-arrow-right-24: API Reference](dokka/schema-kenerator-jsonschema/index.html)


-   __Swagger Schema Generation__

    ---

    `schema-kenerator-swagger`

    Generate Swagger schemas from analyzed types and further customize and enrich schemas with additional steps and annotations.

    [:octicons-arrow-right-24: API Reference](dokka/schema-kenerator-swagger/index.html)

</div>

## Miscellaneous

<div class="grid cards" markdown>

-   __Jackson__

    ---

    `schema-kenerator-jackson`

    Provides steps to support [Jackson](https://github.com/FasterXML/jackson-annotations)-annotations and enrich extracted data independent of generated schema.

    [:octicons-arrow-right-24: API Reference](dokka/schema-kenerator-jackson/index.html)


-   __Jackson JSON Schema__

    ---

    `schema-kenerator-jackson-jsonschema`

    Provides steps to support [Jackson](https://github.com/FasterXML/jackson-annotations)-annotations specific and enrich generated JSON schemas.

    [:octicons-arrow-right-24: API Reference](dokka/schema-kenerator-jackson/index.html)


-   __Jackson Swagger__

    ---

    `schema-kenerator-jackson-swagger`

    Provides steps to support [Jackson](https://github.com/FasterXML/jackson-annotations)-annotations specific and enrich generated Swagger schemas.

    [:octicons-arrow-right-24: API Reference](dokka/schema-kenerator-jackson-swagger/index.html)


-   __Javax/Jakarta Validations Swagger__

    ---

    `schema-kenerator-validation`

    Provides steps to support [Javax](https://mvnrepository.com/artifact/javax.validation/validation-api) and [Jakarta](https://github.com/jakartaee/validation/tree/main) validation annotations when generating Swagger schemas.

    [:octicons-arrow-right-24: API Reference](dokka/schema-kenerator-validation-swagger/index.html)

</div>