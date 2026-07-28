plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinPluginCompose)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    jvmToolchain(26)
}

// Ignore leftover root-owned junk under com/nexus/framework/framework/
// Delete with: sudo rm -rf app/src/main/kotlin/com/nexus/framework/framework
sourceSets.named("main") {
    java.exclude("**/framework/core/**", "**/framework/cli/**")
    kotlin.exclude("**/framework/core/**", "**/framework/cli/**")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(
        "org.jetbrains.compose.components:components-ui-tooling-preview:${libs.versions.composeMultiplatform.get()}",
    )
    implementation(project(":core"))
    implementation(libs.kotlinxSerialization)
    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "com.nexus.framework.AppKt"
    }
}

tasks.test {
    useJUnitPlatform()
}

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
