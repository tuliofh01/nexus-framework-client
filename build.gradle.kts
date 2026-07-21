import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.application.tasks.CreateStartScripts
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    // Catalog plugins (apply false) so project(":…") blocks can apply them without
    // per-module build.gradle.kts. Avoid applying includeBuild convention plugins
    // via apply() from the root — they are not reliably on the root classpath.
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinPluginSerialization) apply false
    alias(libs.plugins.kotlinPluginCompose) apply false
    alias(libs.plugins.composeMultiplatform) apply false
}

/** Shared JDK for :core, :cli, and :app (matches misc/build-logic convention). */
val nexusJvmToolchainVersion = 26

// Type-safe `libs` accessors are only on the root script receiver — nested
// project(":…") { } blocks do not see them. Resolve catalog entries once here.
val libsCatalog = the<VersionCatalogsExtension>().named("libs")
val kotlinxSerializationLib = libsCatalog.findLibrary("kotlinxSerialization").get()
val composeMultiplatformVersion =
    libsCatalog.findVersion("composeMultiplatform").get().requiredVersion

subprojects {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        extensions.configure<KotlinJvmProjectExtension>("kotlin") {
            jvmToolchain(nexusJvmToolchainVersion)
        }
    }

    pluginManager.withPlugin("java") {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.toVersion(nexusJvmToolchainVersion)
            targetCompatibility = JavaVersion.toVersion(nexusJvmToolchainVersion)
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        }
    }
}

// ---------------------------------------------------------------------------
// :core — generation pipeline (sources under core/)
// ---------------------------------------------------------------------------
project(":core") {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    dependencies {
        "implementation"(kotlinxSerializationLib)
        "testImplementation"(kotlin("test"))
    }

    val repoRoot = rootProject.projectDir
    val templateDesktop = repoRoot.resolve("template/desktop-app")
    val templateShared = repoRoot.resolve("template/shared")
    val packToolsDir = templateShared.resolve("tools")
    val packBuildDir = layout.buildDirectory.dir("pack-tools").get().asFile
    val packExe = packBuildDir.resolve("pack_archive")

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
        description = "Pack template/desktop-app/scripts → core/build/lua.dat"
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
        description = "Pack template/desktop-app/python → core/build/python.dat"
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
}

// ---------------------------------------------------------------------------
// :cli — headless generate (sources under cli/)
// ---------------------------------------------------------------------------
project(":cli") {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "application")

    val cliMainClass = "nexus.opensource.framework.cli.FrameworkCliKt"

    extensions.configure<JavaApplication>("application") {
        mainClass.set(cliMainClass)
    }

    tasks.named<JavaExec>("run") {
        workingDir = rootProject.projectDir
        mainClass.set(cliMainClass)
    }

    tasks.withType<CreateStartScripts>().configureEach {
        mainClass.set(cliMainClass)
    }

    dependencies {
        "implementation"(project(":core"))
    }

    tasks.register<JavaExec>("runCli") {
        group = "application"
        description = "Run the Framework CLI with --args=\"…\""
        workingDir = rootProject.projectDir
        classpath = the<SourceSetContainer>().getByName("main").runtimeClasspath
        mainClass.set(cliMainClass)
        if (project.hasProperty("args")) {
            args = (project.property("args") as String).split(" ").filter { it.isNotEmpty() }
        }
    }
}

// ---------------------------------------------------------------------------
// :app — Compose Desktop client (sources under app/)
// ---------------------------------------------------------------------------
project(":app") {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.compose")
    apply(plugin = "org.jetbrains.compose")

    val composeDeps = (dependencies as ExtensionAware).extensions
        .getByName("compose") as ComposePlugin.Dependencies

    dependencies {
        "implementation"(composeDeps.desktop.currentOs)
        // Direct coordinate — composeDeps.components.uiToolingPreview is deprecated.
        "implementation"(
            "org.jetbrains.compose.components:components-ui-tooling-preview:$composeMultiplatformVersion",
        )
        "implementation"(project(":core"))
        "implementation"(kotlinxSerializationLib)
        "testImplementation"(kotlin("test"))
    }

    extensions.configure<ComposeExtension>("compose") {
        (this as ExtensionAware).extensions.configure<DesktopExtension>("desktop") {
            application {
                mainClass = "nexus.opensource.AppKt"
            }
        }
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
}
