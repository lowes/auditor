import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.JSON

apply(from = "../gradle/integration-test.gradle.kts")
apply(from = "../gradle/functional-test.gradle.kts")

object Versions {
    // langauage and frameworks
    const val kotlin = "1.4.20"
    const val springboot = "2.4.0"
    const val jackson = "2.11.2"
    const val reactor = "3.4.0"
    const val reactorKotlinExtension = "1.1.0"
    const val reactorKafka = "1.3.0"
    const val javers = "5.14.0"
    const val auditorClient = "0.0.1-SNAPSHOT"
    // test
    const val kotest = "4.3.1"
    const val mockk = "1.10.2"
    const val testContainers = "1.15.0"
    // codequality
    const val jacoco = "0.8.6"
}

plugins {
    val ktlint = "9.4.1"
    val detekt = "1.14.2"

    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.jlleitschuh.gradle.ktlint") version ktlint
    id("io.gitlab.arturbosch.detekt") version detekt
    `java-library`
    jacoco
    maven
}

// manifest version
group = "com.lowes.auditor"
version = "0.0.1-SNAPSHOT"

// repositories
repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

// dependencies
dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${Versions.springboot}")
    api(project(":client"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
    implementation("org.springframework.boot:spring-boot-starter:${Versions.springboot}")
    testImplementation("org.springframework.boot:spring-boot-test:${Versions.springboot}")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure:${Versions.springboot}")
    testImplementation("io.projectreactor:reactor-test:${Versions.reactor}")
    testImplementation("io.mockk:mockk:${Versions.mockk}")
    testImplementation("io.kotest:kotest-assertions-core:${Versions.kotest}")
    testImplementation("io.kotest:kotest-runner-junit5:${Versions.kotest}")
    testImplementation("io.kotest:kotest-extensions-spring:${Versions.kotest}")
    testImplementation("io.kotest:kotest-property:${Versions.kotest}")
    testImplementation("io.kotest:kotest-extensions-testcontainers:${Versions.kotest}")
    testImplementation("org.testcontainers:kafka:${Versions.testContainers}")
}

// compile
java.sourceCompatibility = JavaVersion.VERSION_14

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=stridevtoolsct")
        jvmTarget = "14"
    }
}

// test
tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    testLogging.showStandardStreams = false
    testLogging.showExceptions = true
    testLogging.showStackTraces = true
    testLogging.showCauses = true
    testLogging.exceptionFormat = FULL
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
    getExecutionData().setFrom(fileTree(buildDir).include("/jacoco/*.exec"))
    reports {
        xml.isEnabled = true
        csv.isEnabled = true
        html.destination = file("$buildDir/jacocoHtml")
    }
}
