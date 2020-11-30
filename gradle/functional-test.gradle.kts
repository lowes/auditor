val sourceSets = the<SourceSetContainer>()

sourceSets {
    create("testFunctional") {
        java.srcDir(file("src/testFunctional/kotlin"))
        resources.srcDir(file("src/testFunctional/resources"))
        compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath
    }
}

tasks.register<Test>("functionalTest") {
    description = "Runs the functional tests."
    group = "verification"
    testClassesDirs = sourceSets["testFunctional"].output.classesDirs
    classpath = sourceSets["testFunctional"].runtimeClasspath
}

tasks.named("check") {
    dependsOn("functionalTest")
}