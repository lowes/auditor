object Versions {
    // language and frameworks
    const val springboot = "2.4.0"
}

plugins {
    kotlin("plugin.spring")
    `java-library`
}

// dependencies
dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${Versions.springboot}")
    api(project(":core"))
    api(project(":client"))
    implementation("org.springframework.boot:spring-boot-starter:${Versions.springboot}")
    testImplementation("org.springframework.boot:spring-boot-test:${Versions.springboot}")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure:${Versions.springboot}")
}
