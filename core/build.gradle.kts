plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinPluginSerialization)
}

kotlin {
    jvmToolchain(26)
}

dependencies {
    implementation(libs.kotlinxSerialization)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

// Host pack tools used when generating projects from templates.
val repoRoot = rootProject.projectDir
val templateDesktop = repoRoot.resolve("template/desktop-app")
val templateShared = repoRoot.resolve("template/shared")
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
    description = "Pack template/desktop-app/scripts → core/build/lua.dat"
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
    description = "Pack template/desktop-app/python → core/build/python.dat"
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
