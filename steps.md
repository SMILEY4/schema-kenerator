# core

### addMissingSupertypeSubtypeRelations

Adds missing subtype-supertype relations between the given types. Types not already present in the input are not included.
Example:
B is a subtype of A. B has A in its list of supertypes, but A not B as a subtype.
This step finds these missing connections and adds them.
Add this step after type analysis and before schema generation.


### handleNameAnnotation

Changes the [TypeData.descriptiveName] to the name specified by a [Name]-annotation.
Add this step after type analysis and before schema generation.


### renameMembers

Renames members of types according to the given function.
Add this step after type analysis and before schema generation.


### gettersToProperties

Merges getters with their matching property:
 - if a matching property exists, the getter will be removed and relevant data copied to the property
 - if no property exists (e.g. because it is private), the getter will be removed and a new property from its data is created
Add this step after type analysis and before schema generation.

### addDiscriminatorProperty


Adds properties to types with subtypes used to differentiate between the possible subtypes when (de-)serializing.
The created property is annotated with a marker annotation with the name [AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME].
If a type already contains a property annotated with the marker annotation, no new property will be added.
Add this step after type analysis and before schema generation.



# reflection

### collectSubTypes

Finds additional subtypes from [SubType]-annotation.
An additional step to add missing subtype-supertype relations .later may be required
Add this step before type analysis.

### analyseTypeUsingReflection

Analyze the type and using reflection and return the extracted data.



# serialization

### addJsonClassDiscriminatorProperty

Handles the [JsonClassDiscriminator]-annotations and adds a discriminator property with the defined name and
annotated with a marker annotation called [io.github.smiley4.schemakenerator.core.AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME]


### renameMembers

Renames members of types according to the given [JsonNamingStrategy].
Add this step after type analysis and before schema generation.


### analyzeTypeUsingKotlinxSerialization

Analyze the type and using kotlinx-serialization and return the extracted data.



# jackson

### collectJacksonSubTypes

Finds and adds additional subtypes from jackson [JsonSubTypes]-annotation.
An additional step to add missing subtype-supertype relations later may be required.
Add this step before any type analysis.


### handleJacksonAnnotations

Handles miscellaneous jackson annotations
- adds support for jackson [JsonIgnore]-annotation and removes annotated members
- adds support for jackson [JsonIgnoreType]-annotation and removes members of the annotated type
- adds support for jackson [JsonIgnoreProperties]-annotation and removes specified members from the annotated types.
- adds support for the jackson [JsonProperty]-annotation.
Renames annotated members and modifies their nullability according to the specified values.
Add this step after type analysis and before schema generation.


### addJacksonTypeInfoDiscriminatorProperty

Handles the [JsonTypeInfo]-annotations and adds a discriminator property with the defined name and
annotated with a marker annotation called [AbstractAddDiscriminatorStep.MARKER_ANNOTATION_NAME].
Add this step after type analysis and before schema generation.



# jackson-jsonschema

### handleJacksonJsonSchemaAnnotations

Adds a description to properties according to the jackson [JsonPropertyDescription]-annotation.
Add this step after schema generation and before schema compilation.



# jackson-swagger

### handleJacksonSwaggerAnnotations

Adds a description to properties according to the jackson [JsonPropertyDescription]-annotation.
Add this step after schema generation and before schema compilation.



# validation swagger

### handleJavaxValidationAnnotations

Adds support for the following Javax Validation annotations:
- [javax.validation.constraints.Max]
- [javax.validation.constraints.Min]
- [javax.validation.constraints.NotBlank]
- [javax.validation.constraints.NotEmpty]
- [javax.validation.constraints.NotNull]
- [javax.validation.constraints.Size]

### handleJakartaValidationAnnotations

Adds support for the following Jakarta Validation annotations:
- [jakarta.validation.constraints.Max]
- [jakarta.validation.constraints.Min]
- [jakarta.validation.constraints.NotBlank]
- [jakarta.validation.constraints.NotEmpty]
- [jakarta.validation.constraints.NotNull]
- [jakarta.validation.constraints.Size]
Add this step after schema generation and before schema compilation.



# jsonschema

### generateJsonSchema

Generates json schemas from the given type data. All types in the schema are provisionally referenced by the full type-id.
Result needs to be "compiled" to get the final json schema.


### withTitle

Adds an automatically determined title to schemas.


### handleCoreAnnotations

Add support for the following schema-kenerator-core annotations:
- [io.github.smiley4.schemakenerator.core.annotations.Optional]
- [io.github.smiley4.schemakenerator.core.annotations.Required]
- [io.github.smiley4.schemakenerator.core.annotations.Default]
- [io.github.smiley4.schemakenerator.core.annotations.Deprecated] and [kotlin.Deprecated]
- [io.github.smiley4.schemakenerator.core.annotations.Description]
- [io.github.smiley4.schemakenerator.core.annotations.Example]
- [io.github.smiley4.schemakenerator.core.annotations.Title]
- [io.github.smiley4.schemakenerator.core.annotations.Format]
- [io.github.smiley4.schemakenerator.core.annotations.Type]
Add this step after schema generation and before schema compilation.


### compileInlining

Resolves references in generated json schemas by inlining them.


### compileReferencing

Resolves references in generated json schemas by collecting them in the components-section and referencing them.


### compileReferencingRoot

Resolves references in generated json schemas by collecting them in the components-section and referencing them.


### customizeTypes

Provide a function that is called for each type and json schema.
Can be used to manually manipulate the generated json schema.


### customizeProperties

Provide a function that is called for each property. Can be used to manually manipulate the generated json schema.



# swagger

### generateSwaggerSchema

Generates swagger schemas from the given type data. All types in the schema are provisionally referenced by the full type-id.
Result needs to be "compiled" to get the final swagger schema.


### withTitle

Adds an automatically determined title to schemas.


### handleCoreAnnotations

Add support for the following schema-kenerator-core annotations:
- [io.github.smiley4.schemakenerator.core.annotations.Optional]
- [io.github.smiley4.schemakenerator.core.annotations.Required]
- [io.github.smiley4.schemakenerator.core.annotations.Default]
- [io.github.smiley4.schemakenerator.core.annotations.Deprecated] and [kotlin.Deprecated]
- [io.github.smiley4.schemakenerator.core.annotations.Description]
- [io.github.smiley4.schemakenerator.core.annotations.Example]
- [io.github.smiley4.schemakenerator.core.annotations.Title]
- [io.github.smiley4.schemakenerator.core.annotations.Format]
- [io.github.smiley4.schemakenerator.core.annotations.Type]
Add this step after schema generation and before schema compilation.


### handleSchemaAnnotations

Add support for the following swagger annotations:
- [io.swagger.v3.oas.annotations.media.Schema]
     - on types
          - title
          - description
     - on properties
          - name
          - title
          - description
          - example
          - hidden
          - allowableValues
          - defaultValue
          - accessMode
          - minLength
          - maxLength,
          - format
          - minimum
          - maximum
          - exclusiveMaximum
          - exclusiveMinimum
- [io.swagger.v3.oas.annotations.media.ArraySchema]
     - minItems
     - maxItems
     - uniqueItems
Add this step after schema generation and before schema compilation.


### mergePropertyAttributesIntoType

Merge the attributes of a property into the referenced type.


### compileInlining

Resolves references in generated swagger schemas by inlining them.


### compileReferencing

Resolves references in generated swagger schemas by collecting them in the components-section and referencing them.


### compileReferencingRoot

Resolves references in generated swagger schemas by collecting them in the components-section and referencing them.


### customizeTypes

Provide a function that is called for each type and swagger schema.
Can be used to manually manipulate the generated swagger schema.


### customizeProperties

Provide a function that is called for each property. Can be used to manually manipulate the generated swagger schema.
