// dependencies
dependencies {
    api(project(":core"))
    implementation("org.apache.logging.log4j:log4j-core:_")
    implementation("org.apache.logging.log4j:log4j-api:_")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:_")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:_")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner:_")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:_")
    implementation("io.projectreactor.kafka:reactor-kafka:_")
}
