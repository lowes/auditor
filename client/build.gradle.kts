object Versions {
    // languages and frameworks
    const val jackson = "2.12.0"
    const val reactorKafka = "1.3.0"
    const val javers = "5.14.0"
}

// dependencies
dependencies {
    implementation(project(":core"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner:${Versions.jackson}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jackson}")
    implementation("io.projectreactor.kafka:reactor-kafka:${Versions.reactorKafka}")
    implementation("org.javers:javers-core:${Versions.javers}")
}

frameworkModule {
    application.enabled.set(false)
    springboot.enabled.set(false)
}
