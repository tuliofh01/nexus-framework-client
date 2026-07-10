plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

val repoRoot = rootProject.projectDir
val templateDesktop = repoRoot.resolve("template/desktop-app")
val templateShared = repoRoot.resolve("template/shared")
val packToolsDir = templateShared.resolve("tools")
val packBuildDir = layout.buildDirectory.dir("pack-tools").get().asFile
val packExe = packBuildDir.resolve("pack_archive")

dependencies {
    implementation(libs.kotlinxSerialization)
    testImplementation(kotlin("test"))
}

tasks.register<Exec>("configurePackArchive") {
    group = "nexus"
    description = "Configure host pack_archive tool (template/shared/tools)"
    workingDir = packToolsDir
    inputs.dir(templateShared.resolve("runtime"))
    inputs.dir(packToolsDir)
    commandLine(
        "cmake",
        "-B",
        packBuildDir.absolutePath,
        "-S",
        packToolsDir.absolutePath,
    )
    outputs.dir(packBuildDir)
}

tasks.register<Exec>("buildPackArchive") {
    group = "nexus"
    description = "Build host pack_archive tool from template/shared/tools"
    dependsOn("configurePackArchive")
    commandLine(
        "cmake",
        "--build",
        packBuildDir.absolutePath,
        "--target",
        "pack_archive",
        "-j",
    )
    outputs.file(packExe)
}

tasks.register<Exec>("packTemplateLuaDat") {
    group = "nexus"
    description = "Pack template/desktop-app/scripts → misc/core/build/lua.dat"
    dependsOn("buildPackArchive")
    val out = layout.buildDirectory.file("lua.dat").get().asFile
    outputs.file(out)
    commandLine(
        packExe.absolutePath,
        "lua",
        templateDesktop.resolve("scripts").absolutePath,
        out.absolutePath,
    )
}

tasks.register<Exec>("packTemplatePythonDat") {
    group = "nexus"
    description = "Pack template/desktop-app/python → misc/core/build/python.dat"
    dependsOn("buildPackArchive")
    val out = layout.buildDirectory.file("python.dat").get().asFile
    outputs.file(out)
    commandLine(
        packExe.absolutePath,
        "python",
        templateDesktop.resolve("python").absolutePath,
        out.absolutePath,
    )
}
