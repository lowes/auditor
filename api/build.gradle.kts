

object Versions {
    // languages and frameworks
    const val jackson = "2.11.2"
    const val reactor = "3.4.0"
    const val reactorKafka = "1.3.0"
    const val javers = "5.14.0"
}

plugins {
    `java-library`
}

// dependencies
dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner:${Versions.jackson}")
    implementation("io.projectreactor:reactor-core:${Versions.reactor}")
    implementation("io.projectreactor.kafka:reactor-kafka:${Versions.reactorKafka}")
    implementation("org.javers:javers-core:${Versions.javers}")
    testImplementation("io.projectreactor:reactor-test:${Versions.reactor}")
}
