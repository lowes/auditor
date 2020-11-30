val sourceSets = the<SourceSetContainer>()

sourceSets {
    create("testIntegration") {
        java.srcDir(file("src/testIntegration/kotlin"))
        resources.srcDir(file("src/testIntegration/resources"))
        compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests."
    group = "verification"
    testClassesDirs = sourceSets["testIntegration"].output.classesDirs
    classpath = sourceSets["testIntegration"].runtimeClasspath
}

tasks.named("check") {
    dependsOn("integrationTest")
}