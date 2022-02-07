tasks.jar {
    archiveBaseName.set("auditor-client")
}

// dependencies
dependencies {
    api(project(":core"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:_")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner:_")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:_")
    implementation("io.projectreactor.kafka:reactor-kafka:_")
}
