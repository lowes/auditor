object Versions {
    // language and frameworks
    const val springboot = "2.4.0"
}

plugins {
    `java-library`
}

// dependencies
dependencies {
    api(project(":core"))
    api(project(":client"))
    implementation("org.springframework.boot:spring-boot-starter:${Versions.springboot}")
    testImplementation("org.springframework.boot:spring-boot-test:${Versions.springboot}")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure:${Versions.springboot}")
}

frameworkModule {
    springboot.enabled.set(false)
}
