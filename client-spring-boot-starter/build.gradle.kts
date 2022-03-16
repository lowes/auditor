plugins {
    `java-library`
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
