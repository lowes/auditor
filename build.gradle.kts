import groovy.lang.GroovyObject
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.JSON

apply(from = "$rootDir/gradle/integration-test.gradle.kts")
apply(from = "$rootDir/gradle/functional-test.gradle.kts")

object Versions {
    // language and frameworks
    const val kotlin = "1.4.30"
    const val reactor = "3.4.3"
    // test
    const val kotest = "4.4.1"
    const val mockk = "1.10.6"
    const val testContainers = "1.15.2"
    // codequality
    const val jacoco = "0.8.6"
}

plugins {
    val kotlin = "1.4.30"
    val openApiGenerator = "5.3.1"
    val ktlint = "9.4.1"
    val detekt = "1.15.0"
    val artifactory = "4.26.2"
    val nebulaRelease = "15.3.0"
    idea
    jacoco
    `maven-publish`
    kotlin("jvm") version kotlin
    id("org.jlleitschuh.gradle.ktlint") version ktlint
    id("io.gitlab.arturbosch.detekt") version detekt
    id("com.jfrog.artifactory") version artifactory
    id("nebula.release") version nebulaRelease
    kotlin("plugin.spring") version kotlin apply false
    id("org.openapi.generator") version openApiGenerator apply false
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
        implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
        implementation("io.projectreactor:reactor-core:${Versions.reactor}")
        testImplementation("io.projectreactor:reactor-test:${Versions.reactor}")
        testImplementation("io.mockk:mockk:${Versions.mockk}")
        testImplementation("io.kotest:kotest-assertions-core:${Versions.kotest}")
        testImplementation("io.kotest:kotest-runner-junit5:${Versions.kotest}")
        testImplementation("io.kotest:kotest-extensions-spring:${Versions.kotest}")
        testImplementation("io.kotest:kotest-property:${Versions.kotest}")
        testImplementation("io.kotest:kotest-extensions-testcontainers:${Versions.kotest}")
        testImplementation("org.testcontainers:kafka:${Versions.testContainers}")
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
        reports {
            html.enabled = true // observe findings in your browser with structure and code snippets
            txt.enabled = true // similar to the console output, contains issue signature to manually edit baseline files
            xml.enabled = true
        }
    }

    tasks {
        withType<Detekt> {
            this.jvmTarget = "14"
        }
    }

    // jacoco
    jacoco {
        toolVersion = Versions.jacoco
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.withType(Test::class.java))
        executionData.setFrom(fileTree(buildDir).include("/jacoco/*.exec"))
        reports {
            xml.isEnabled = true
            csv.isEnabled = true
            html.destination = file("$buildDir/jacocoHtml")
        }
    }

    // publish
    artifactory {
        setContextUrl("<<maven-repo-url-here>>")
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
