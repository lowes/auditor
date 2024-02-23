// dependencies
dependencies {
    implementation(project(":client"))
    implementation("org.apache.logging.log4j:log4j-api:_")
    implementation("org.apache.logging.log4j:log4j-core:_")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:_")
}

tasks.withType<Javadoc>().all { enabled = false }
