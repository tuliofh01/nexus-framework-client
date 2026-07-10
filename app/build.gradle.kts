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

    implementation(project(":core"))

    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        // Entry point that wires the MVC layers together.
        // (Kotlin compiles `App.kt` to a class with FQN `nexus.opensource.AppKt`.)
        mainClass = "nexus.opensource.AppKt"
    }
}

// Repo-root builds/client/ — see builds/README.md
val buildsClientDir = rootProject.layout.projectDirectory.dir("builds/client")
val composeBinariesDir = layout.buildDirectory.dir("compose/binaries/main")

tasks.register<Sync>("deployToBuildsClient") {
    group = "distribution"
    description = "Copy the Compose Desktop distributable into builds/client/app/"
    dependsOn("createDistributable")
    from(composeBinariesDir.map { it.dir("app") })
    into(buildsClientDir.dir("app"))
}

tasks.register<Sync>("deployPackageToBuildsClient") {
    group = "distribution"
    description = "Copy OS packages from packageDistributionForCurrentOS into builds/client/packages/"
    dependsOn("packageDistributionForCurrentOS")
    from(composeBinariesDir) {
        include("**/*.deb", "**/*.rpm", "**/*.dmg", "**/*.msi", "**/*.exe", "**/*.pkg")
    }
    into(buildsClientDir.dir("packages"))
}
