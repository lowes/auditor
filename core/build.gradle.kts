import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.openapi.generator")
    `java-library`
}

// dependencies
dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:_")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner:_")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:_")
}

val openApiConfigMap =
    mapOf(
        "dateLibrary" to "java8",
        "enumPropertyNaming" to "original",
        "serializationLibrary" to "jackson",
    )

val openApiSystemProperties =
    mapOf(
        "apis" to "false",
        "models" to "",
        "apiTests" to "false",
        "modelDocs" to "false",
        "modelTests" to "false",
    )

val openApiTypeMappings =
    mapOf(
        "array" to "kotlin.collections.List",
    )

tasks.create("openApiGenerateEventDTO", GenerateTask::class) {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/specs/auditor-v1-event-DTO.yaml")
    outputDir.set("$buildDir/generated")
    skipOverwrite.set(true)
    modelPackage.set("com.lowes.auditor.core.infrastructure.event.model")
    configOptions.set(openApiConfigMap)
    globalProperties.set(openApiSystemProperties)
    typeMappings.set(openApiTypeMappings)
}

tasks.create("openApiGenerateEntities", GenerateTask::class) {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/specs/auditor-v1-entities.yaml")
    outputDir.set("$buildDir/generated")
    skipOverwrite.set(true)
    modelPackage.set("com.lowes.auditor.core.entities.domain")
    configOptions.set(openApiConfigMap)
    globalProperties.set(openApiSystemProperties)
    typeMappings.set(openApiTypeMappings)
}

sourceSets {
    val main by getting
    main.java.srcDirs("$buildDir/generated/src/main/kotlin")
}

tasks.withType<KotlinCompile> {
    dependsOn("openApiGenerateEntities")
    dependsOn("openApiGenerateEventDTO")
}
