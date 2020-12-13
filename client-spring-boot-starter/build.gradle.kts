object Versions {
    // language and frameworks
    const val springboot = "2.4.0"
    const val auditorClient = "0.0.1-SNAPSHOT"
}

plugins {
    kotlin("plugin.spring")
    `java-library`
}

tasks.jar {
    archiveBaseName.set("auditor-client-spring-boot-starter")
}

// dependencies
dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${Versions.springboot}")
    api(project(":client"))
    implementation("org.springframework.boot:spring-boot-starter:${Versions.springboot}")
    testImplementation("org.springframework.boot:spring-boot-test:${Versions.springboot}")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure:${Versions.springboot}")
}
