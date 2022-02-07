object Versions {
    // languages and frameworks
    const val jackson = "2.12.1"
    const val reactorKafka = "1.3.2"
}

tasks.jar {
    archiveBaseName.set("auditor-client")
}

// dependencies
dependencies {
    api(project(":core"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner:${Versions.jackson}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jackson}")
    implementation("io.projectreactor.kafka:reactor-kafka:${Versions.reactorKafka}")
}
