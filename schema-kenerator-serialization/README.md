# Schema-Kenerator Reflection

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-reflection/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.smiley4/schema-kenerator-reflection)

This project provides steps to extract information from kotlin types using kotlinx-serialization.

## Steps

| Step                            | Description                                                                                                                                                                                 |
|---------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `processKotlinxSerialization()` | Analyze the types and extract information.<br/>**Configuration:** <br/> - *custom processors*: overwrite the default behavior for specific types and implement custom information extractor |

