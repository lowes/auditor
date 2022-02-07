object Versions {
    const val log4j = "2.17.1"
}
// dependencies
dependencies {
    implementation(project(":client"))
    implementation("org.apache.logging.log4j:log4j-api:${Versions.log4j}")
    implementation("org.apache.logging.log4j:log4j-core:${Versions.log4j}")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4j}")
}
