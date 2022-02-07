object Versions {
    // language and frameworks
    const val springboot = "2.4.3"
}

// dependencies
dependencies {
    implementation(project(":client-spring-boot-starter"))
    implementation("org.springframework.boot:spring-boot-starter:_")
}
