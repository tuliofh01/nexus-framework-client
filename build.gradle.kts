plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.kotlinPluginCompose)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    jvmToolchain(26)

    sourceSets.named("main") {
        // Root-owned leftover junk from nested-module experiments (purge with sudo).
        kotlin.exclude("**/com/nexus/framework/framework/**")
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(
        "org.jetbrains.compose.components:components-ui-tooling-preview:${libs.versions.composeMultiplatform.get()}",
    )
    implementation(libs.kotlinxSerialization)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

// ---------------------------------------------------------------------------
// Compose Desktop client — ./gradlew run
// ---------------------------------------------------------------------------
compose.desktop {
    application {
        mainClass = "com.nexus.framework.AppKt"
    }
}

val buildsClientDir = layout.projectDirectory.dir("builds/client")
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

// ---------------------------------------------------------------------------
// CLI — ./gradlew runCli --args="generate --type desktop --name MyApp"
// ---------------------------------------------------------------------------
val cliMainClass = "com.nexus.framework.cli.FrameworkCliKt"

tasks.register<JavaExec>("runCli") {
    group = "application"
    description = "Run the Framework CLI (generate / import-langflow)"
    workingDir = rootProject.projectDir
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(cliMainClass)
    if (project.hasProperty("args")) {
        args = (project.property("args") as String).split(" ").filter { it.isNotEmpty() }
    }
}

// ---------------------------------------------------------------------------
// Host pack tools (template/shared/tools → lua.dat / python.dat)
// ---------------------------------------------------------------------------
val templateDesktop = rootProject.projectDir.resolve("template/desktop-app")
val templateShared = rootProject.projectDir.resolve("template/shared")
val packToolsDir = templateShared.resolve("tools")
val packBuildDir = layout.buildDirectory.dir("pack-tools")
val packExe = packBuildDir.map { it.asFile.resolve("pack_archive") }

tasks.register<Exec>("configurePackArchive") {
    group = "nexus"
    description = "Configure host pack_archive tool (template/shared/tools)"
    workingDir = packToolsDir
    inputs.dir(templateShared.resolve("runtime"))
    inputs.dir(packToolsDir)
    commandLine("cmake", "-B", packBuildDir.get().asFile.absolutePath, "-S", packToolsDir.absolutePath)
    outputs.dir(packBuildDir)
}

tasks.register<Exec>("buildPackArchive") {
    group = "nexus"
    description = "Build host pack_archive tool from template/shared/tools"
    dependsOn("configurePackArchive")
    commandLine("cmake", "--build", packBuildDir.get().asFile.absolutePath, "--target", "pack_archive", "-j")
    outputs.file(packExe)
}

tasks.register<Exec>("packTemplateLuaDat") {
    group = "nexus"
    description = "Pack template/desktop-app/scripts → build/lua.dat"
    dependsOn("buildPackArchive")
    val out = layout.buildDirectory.file("lua.dat")
    outputs.file(out)
    commandLine(
        packExe.get().absolutePath,
        "lua",
        templateDesktop.resolve("scripts").absolutePath,
        out.get().asFile.absolutePath,
    )
}

tasks.register<Exec>("packTemplatePythonDat") {
    group = "nexus"
    description = "Pack template/desktop-app/python → build/python.dat"
    dependsOn("buildPackArchive")
    val out = layout.buildDirectory.file("python.dat")
    outputs.file(out)
    commandLine(
        packExe.get().absolutePath,
        "python",
        templateDesktop.resolve("python").absolutePath,
        out.get().asFile.absolutePath,
    )
}
