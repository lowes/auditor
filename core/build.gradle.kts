import com.lowes.iss.gradleplugins.OpenApiModuleConfig

object Versions {
    // languages and frameworks
    const val jackson = "2.12.0"
}

plugins {
    `java-library`
}

// dependencies
dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner:${Versions.jackson}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jackson}")
}

generatorModule {
    openApi.enabled.set(true)
    openApi.config.set(
        listOf(
            OpenApiModuleConfig(
                inputSpec = "$rootDir/specs/auditor-v1-event-DTO.yaml",
                modelPackage = "com.lowes.auditor.core.infrastructure.event.model"
            ),
            OpenApiModuleConfig(
                inputSpec = "$rootDir/specs/auditor-v1-entities.yaml",
                modelPackage = "com.lowes.auditor.core.entities.domain"
            )
        )
    )
}
