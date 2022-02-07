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
    implementation("ch.qos.logback:logback-core:_")
    implementation("ch.qos.logback:logback-classic:_")
    implementation("org.springframework.boot:spring-boot-starter:_")
    testImplementation("org.springframework.boot:spring-boot-test:_")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure:_")
}
