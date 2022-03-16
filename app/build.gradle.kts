plugins {
    id("org.springframework.boot")
}

// dependencies
dependencies {
    api(project(":core"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:_")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner:_")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:_")
    implementation("org.springframework.boot:spring-boot-starter:_")
    implementation("io.projectreactor.kafka:reactor-kafka:_")
    implementation("org.springframework.data:spring-data-elasticsearch:_")
    implementation("org.springframework:spring-web:_")
}

project.configurations {
    listOf(get("apiElements"), get("runtimeElements")).forEach { config ->
        config.outgoing.artifacts.removeIf {
            it.buildDependencies
                .getDependencies(null)
                .contains(project.tasks.named("jar", Jar::class.java).get())
        }
        project.tasks.findByName("bootJar")?.let { config.outgoing.artifact(it) }
    }
}
