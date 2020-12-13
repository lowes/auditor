import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

object Versions {
    // languages and frameworks
    const val jackson = "2.11.2"
    const val reactor = "3.4.0"
    const val reactorKafka = "1.3.0"
    const val javers = "5.14.0"
}

plugins {
    id("org.openapi.generator")
}

tasks.jar {
    archiveBaseName.set("auditor-client")
}

// dependencies
dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner:${Versions.jackson}")
    implementation("io.projectreactor:reactor-core:${Versions.reactor}")
    implementation("io.projectreactor.kafka:reactor-kafka:${Versions.reactorKafka}")
    implementation("org.javers:javers-core:${Versions.javers}")
    testImplementation("io.projectreactor:reactor-test:${Versions.reactor}")
}

val openApiConfigMap = mapOf(
    "dateLibrary" to "java8",
    "enumPropertyNaming" to "original",
    "serializationLibrary" to "jackson"
)

val openApiSystemProperties = mapOf(
    "apis" to "false",
    "models" to "",
    "apiTests" to "false",
    "modelDocs" to "false",
    "modelTests" to "false"
)

val openApiTypeMappings = mapOf(
    "array" to "kotlin.collections.List"
)

tasks.create("openApiGenerateEventDTO", GenerateTask::class) {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/specs/auditor-v1-event-DTO.yaml")
    outputDir.set("$buildDir/generated")
    skipOverwrite.set(true)
    modelPackage.set("com.lowes.auditor.client.infrastructure.event.model")
    configOptions.set(openApiConfigMap)
    systemProperties.set(openApiSystemProperties)
    typeMappings.set(openApiTypeMappings)
}

tasks.create("openApiGenerateEntities", GenerateTask::class) {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/specs/auditor-v1-entities.yaml")
    outputDir.set("$buildDir/generated")
    skipOverwrite.set(true)
    modelPackage.set("com.lowes.auditor.client.entities.domain")
    configOptions.set(openApiConfigMap)
    systemProperties.set(openApiSystemProperties)
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
