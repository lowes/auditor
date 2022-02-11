plugins {
    id("org.springframework.boot")
}

// dependencies
dependencies {
    implementation(project(":client-spring-boot-starter"))
    implementation("org.springframework.boot:spring-boot-starter:_")
}
