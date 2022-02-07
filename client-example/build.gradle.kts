object Versions {
    const val log4j = "2.17.1"
}
// dependencies
dependencies {
    implementation(project(":client"))
    implementation("org.apache.logging.log4j:log4j-api:_")
    implementation("org.apache.logging.log4j:log4j-core:_")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:_")
}
