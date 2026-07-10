plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

val repoRoot = rootProject.projectDir
val templateDesktop = repoRoot.resolve("template/desktop-app")
val templateShared = repoRoot.resolve("template/shared")
val packToolsDir = templateShared.resolve("tools")
val packBuildDir = layout.buildDirectory.dir("pack-tools")
val packExeProvider = packBuildDir.map { it.file("pack_archive").asFile.absolutePath }

dependencies {
    implementation(libs.kotlinxSerialization)
    testImplementation(kotlin("test"))
}

tasks.register("buildPackArchive") {
    group = "nexus"
    description = "Build host pack_archive tool from desktop template CMake"
    val buildDir = packBuildDir.get().asFile
    val exe = buildDir.resolve("pack_archive")
    inputs.dir(templateShared.resolve("runtime"))
    inputs.dir(packToolsDir)
    outputs.file(exe)

    doLast {
        if (!exe.exists()) {
            fun run(vararg args: String) {
                check(
                    ProcessBuilder(*args)
                        .directory(packToolsDir)
                        .inheritIO()
                        .start()
                        .waitFor() == 0,
                ) { "Command failed: ${args.joinToString(" ")}" }
            }
            run("cmake", "-B", buildDir.absolutePath, "-S", packToolsDir.absolutePath)
            run("cmake", "--build", buildDir.absolutePath, "--target", "pack_archive", "-j")
        }
    }
}

tasks.register<Exec>("packTemplateLuaDat") {
    group = "nexus"
    description = "Pack template/desktop-app/scripts → misc/core/build/lua.dat"
    dependsOn("buildPackArchive")
    val out = layout.buildDirectory.file("lua.dat")
    outputs.file(out)
    commandLine(
        packExeProvider.get(),
        "lua",
        templateDesktop.resolve("scripts").absolutePath,
        out.get().asFile.absolutePath,
    )
}

tasks.register<Exec>("packTemplatePythonDat") {
    group = "nexus"
    description = "Pack template/desktop-app/python → misc/core/build/python.dat"
    dependsOn("buildPackArchive")
    val out = layout.buildDirectory.file("python.dat")
    outputs.file(out)
    commandLine(
        packExeProvider.get(),
        "python",
        templateDesktop.resolve("python").absolutePath,
        out.get().asFile.absolutePath,
    )
}
