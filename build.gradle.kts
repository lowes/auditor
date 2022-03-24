import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.JSON

apply(from = "$rootDir/gradle/integration-test.gradle.kts")
apply(from = "$rootDir/gradle/functional-test.gradle.kts")

plugins {
    idea
    jacoco
    `maven-publish`
    signing
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    kotlin("plugin.spring") apply false
    id("org.openapi.generator") apply false
    id("org.springframework.boot") apply false
}

// repositories
repositories {
    mavenLocal()
    mavenCentral()
}

subprojects {
    apply(plugin = "org.gradle.idea")
    apply(plugin = "org.gradle.signing")
    apply(plugin = "org.gradle.jacoco")
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(from = "$rootDir/gradle/integration-test.gradle.kts")
    apply(from = "$rootDir/gradle/functional-test.gradle.kts")

    // repositories
    repositories {
        mavenLocal()
        mavenCentral()
    }

    // compile
    java.sourceCompatibility = JavaVersion.VERSION_11

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            languageVersion = "1.5"
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect:_")
        implementation(Kotlin.stdlib.jdk8)
        implementation("io.projectreactor:reactor-core:_")
        testImplementation("${Spring.reactor.test}:_")
        testImplementation(Testing.mockK)
        testImplementation(Testing.kotest.assertions.core)
        testImplementation(Testing.kotest.runner.junit5)
        testImplementation(Testing.kotest.property)
        testImplementation(Testing.kotestExtensions.spring)
        testImplementation(Testing.kotestExtensions.testContainers)
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

    if (!project.name.contains("example")) {
        publishing {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                    afterEvaluate {
                        artifactId = "auditor-".plus(tasks.jar.get().archiveBaseName.get())
                        group = "io.github.lowes"
                        version = getAuditorVersion()
                    }
                    pom {
                        name.set("Auditor")
                        description.set("Auditing Library for JVM apps")
                        url.set("https://github.com/lowes/auditor")
                        licenses {
                            license {
                                name.set("Apache-2.0")
                                url.set("https://opensource.org/licenses/Apache-2.0")
                            }
                        }
                        developers {
                            developer {
                                id.set("lowesoss")
                                name.set("Lowe's Home Improvement")
                                organization.set("Lowe's")
                                organizationUrl.set("https://www.lowes.com")
                            }
                        }
                        scm {
                            url.set(
                                "https://github.com/lowes/auditor.git"
                            )
                            connection.set(
                                "scm:git:git://github.com/lowes/auditor.git"
                            )
                            developerConnection.set(
                                "scm:git:git://github.com/lowes/auditor.git"
                            )
                        }
                        issueManagement {
                            url.set("https://github.com/lowes/auditor/issues")
                        }
                    }
                }
            }
            repositories {
                maven {
                    name = "snapshot"
                    setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots")
                    credentials {
                        username = System.getenv("OSSRH_USER") ?: project.properties["ossrhUsername"].toString()
                        password = System.getenv("OSSRH_PASSWORD") ?: project.properties["ossrhPassword"].toString()
                    }
                }
                maven {
                    name = "release"
                    setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                    credentials {
                        username = System.getenv("OSSRH_USER") ?: project.properties["ossrhUsername"].toString()
                        password = System.getenv("OSSRH_PASSWORD") ?: project.properties["ossrhPassword"].toString()
                    }
                }
            }
        }

        signing {
            isRequired = true
            sign(publishing.publications["maven"])
        }
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    // Test tasks
    val tasksNames: MutableList<String> = gradle.startParameter.taskNames
    if (tasksNames.contains("integrationTest") && !tasksNames.contains("functionalTest") && !tasksNames.contains("test")) {
        gradle.startParameter.excludedTaskNames += setOf("functionalTest", "test")
    } else if (tasksNames.contains("functionalTest") && !tasksNames.contains("integrationTest") && !tasksNames.contains(
            "test"
        )
    ) {
        gradle.startParameter.excludedTaskNames += setOf("integrationTest", "test")
    } else if (tasksNames.contains("test") && !tasksNames.contains("integrationTest") && !tasksNames.contains("functionalTest")) {
        gradle.startParameter.excludedTaskNames += setOf("integrationTest", "functionalTest")
    }
}

fun getAuditorVersion(): String {
    val version = System.getenv().getOrDefault("AUDITOR_VERSION", "v0.0.1")
    val finalVersion = if(version.startsWith('v')) version.drop(1) else version
    return if (gradle.startParameter.taskNames.any { it.contains("Snapshot") }) finalVersion.plus("-SNAPSHOT") else finalVersion
}
