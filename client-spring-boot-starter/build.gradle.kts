object Versions {
    // language and frameworks
    const val springboot = "2.4.3"
    const val logback = "1.2.10"
}

plugins {
    `java-library`
}

tasks.jar {
    archiveBaseName.set("auditor-client-spring-boot-starter")
}

// dependencies
dependencies {
    api(project(":core"))
    api(project(":client"))
    implementation("ch.qos.logback:logback-core:${Versions.logback}")
    implementation("ch.qos.logback:logback-classic:${Versions.logback}")
    implementation("org.springframework.boot:spring-boot-starter:${Versions.springboot}")
    testImplementation("org.springframework.boot:spring-boot-test:${Versions.springboot}")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure:${Versions.springboot}")
}
