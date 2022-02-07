import groovy.lang.GroovyObject
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.JSON

apply(from = "$rootDir/gradle/integration-test.gradle.kts")
apply(from = "$rootDir/gradle/functional-test.gradle.kts")

plugins {
    idea
    jacoco
    `maven-publish`
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    id("com.jfrog.artifactory")
    id("nebula.release")
    kotlin("plugin.spring") apply false
    id("org.openapi.generator") apply false
}

// repositories
repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

subprojects {
    apply(plugin = "org.gradle.idea")
    apply(plugin = "org.gradle.jacoco")
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "com.jfrog.artifactory")
    apply(plugin = "nebula.release")
    apply(from = "$rootDir/gradle/integration-test.gradle.kts")
    apply(from = "$rootDir/gradle/functional-test.gradle.kts")

    // repositories
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }

    // compile
    java.sourceCompatibility = JavaVersion.VERSION_11

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.ExperimentalStdlibApi")
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect:_")
        implementation(Kotlin.stdlib.jdk8)
        implementation("io.projectreactor:reactor-core:_")
        testImplementation("io.projectreactor:reactor-test:_")
        testImplementation(Testing.mockK)
        testImplementation(Testing.kotest.assertions.core)
        testImplementation(Testing.kotest.runner.junit5)
        testImplementation(Testing.kotest.extensions.spring)
        testImplementation(Testing.kotest.property)
        testImplementation(Testing.kotest.extensions.testContainers)
        testImplementation("org.testcontainers:kafka:_")
    }

    // test
    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
        testLogging.showStandardStreams = false
        testLogging.showExceptions = true
        testLogging.showStackTraces = true
        testLogging.showCauses = true
        testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    // code quality: ktlint, detekt, jacoco

    // ktlint
    ktlint {
        filter {
            exclude { element -> element.file.path.contains("generated/") }
            include("**/kotlin/**")
        }
        verbose.set(true)
        outputToConsole.set(true)
        coloredOutput.set(true)
        reporters {
            reporter(JSON)
            reporter(CHECKSTYLE)
            reporter(HTML)
        }
    }

    // detekt
    detekt {
        buildUponDefaultConfig = true // preconfigure defaults
        config = files("$rootDir/gradle/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
    }

    tasks {
        withType<Detekt> {
            this.jvmTarget = "14"
            reports {
                html.required.set(true) // observe findings in your browser with structure and code snippets
                xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
                txt.required.set(true) // similar to the console output, contains issue signature to manually edit baseline files
                sarif.required.set(true) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with Github Code Scanning
            }
        }
    }

    // jacoco
    jacoco {
        toolVersion = "0.8.7"
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.withType(Test::class.java))
        executionData.setFrom(fileTree(buildDir).include("/jacoco/*.exec"))
        reports {
            xml.required.set(true)
            csv.required.set(true)
            html.required.set(true)
            html.outputLocation.set(file("$buildDir/jacocoHtml"))
        }
    }

    // publish
    artifactory {
        setContextUrl("<<//todo maven-repo-url-here>>")
        publish(
            delegateClosureOf<PublisherConfig> {
                repository(
                    delegateClosureOf<GroovyObject> {
                        setProperty("repoKey", getArtifactoryRepo(project.version.toString()))
                        setProperty("username", project.findProperty("artifactory_user") ?: "dummy_user")
                        setProperty("password", project.findProperty("artifactory_password") ?: "dummy_password")
                        setProperty("maven", true)
                    }
                )
                defaults(
                    delegateClosureOf<GroovyObject> {
                        invokeMethod("publications", "mavenJava")
                    }
                )
            }
        )
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                afterEvaluate {
                    artifactId = tasks.jar.get().archiveBaseName.get()
                    group = "com.lowes.auditor"
                }
            }
        }
    }

    // Test tasks
    val tasksNames: MutableList<String> = gradle.startParameter.taskNames
    if (tasksNames.contains("integrationTest") && !tasksNames.contains("functionalTest") && !tasksNames.contains("test")) {
        gradle.startParameter.excludedTaskNames += setOf("functionalTest", "test")
    } else if (tasksNames.contains("functionalTest") && !tasksNames.contains("integrationTest") && !tasksNames.contains("test")) {
        gradle.startParameter.excludedTaskNames += setOf("integrationTest", "test")
    } else if (tasksNames.contains("test") && !tasksNames.contains("integrationTest") && !tasksNames.contains("functionalTest")) {
        gradle.startParameter.excludedTaskNames += setOf("integrationTest", "functionalTest")
    }
}

fun getArtifactoryRepo(version: String): String = if (version.contains("-dev")) "snapshots" else "releases"
