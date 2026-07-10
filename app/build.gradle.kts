plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Jetpack Compose (Compose Multiplatform for Desktop) + the Kotlin Compose compiler plugin.
    // Versions come from the version catalog (`gradle/libs.versions.toml`).
    alias(libs.plugins.kotlinPluginCompose)
    alias(libs.plugins.composeMultiplatform)
}

dependencies {
    // Compose Desktop runtime for the current OS (includes desktop @Preview tooling support).
    implementation(compose.desktop.currentOs)

    // Multiplatform @Preview annotation so the IDE's Compose UI designer/preview panel works.
    implementation(compose.components.uiToolingPreview)

    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        // Entry point that wires the MVC layers together.
        // (Kotlin compiles `App.kt` to a class with FQN `nexus.opensource.AppKt`.)
        mainClass = "nexus.opensource.AppKt"
    }
}
