plugins {
    id("com.android.application")
    id("com.chaquo.python")
    id("org.jetbrains.kotlin.android")
}

val templateRoot = rootProject.projectDir
val generatedAssets = layout.buildDirectory.dir("generated/nxs-assets")
val hostPackDir = layout.buildDirectory.dir("host-pack")
val packExe = hostPackDir.map { it.file("pack_archive") }
val luaOut = layout.buildDirectory.file("assets/lua.dat")
val nxsConfig = file("$templateRoot/nxs_config.json")

android {
    namespace = "com.nexus.{{packageName}}"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nexus.{{packageName}}"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++20 -Wall"
                arguments += listOf("-DANDROID_STL=c++_shared")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    externalNativeBuild {
        cmake {
            path = file("../CMakeLists.txt")
            version = "3.24.0+"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    val generatedAssets = layout.buildDirectory.dir("generated/nxs-assets")

    sourceSets {
        getByName("main") {
            kotlin.srcDirs(
                "src/main/java",
                "$templateRoot/djinni-generated/kotlin",
            )
            assets.srcDirs(
                layout.buildDirectory.dir("assets"),
                "$templateRoot/scripts",
                "$templateRoot/ui",
                generatedAssets,
            )
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

chaquopy {
    defaultConfig {
        version = "3.11"
        pip {
            install("numpy")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}

tasks.register<Copy>("copyBlueprintAsset") {
    group = "nexus"
    description = "Stage blueprint.json into generated APK assets"
    from("$templateRoot/blueprint.json")
    into(generatedAssets)
}

tasks.register<Exec>("packLuaDat") {
    group = "nexus"
    description = "Pack Lua scripts into build/assets/lua.dat before APK merge"
    workingDir = templateRoot
    inputs.dir(file("$templateRoot/scripts"))
    if (nxsConfig.exists()) {
        inputs.file(nxsConfig)
    }
    outputs.file(luaOut)

    doFirst {
        luaOut.get().asFile.parentFile.mkdirs()
        if (!packExe.get().asFile.exists()) {
            exec {
                commandLine(
                    "cmake", "-B", hostPackDir.get().asFile.absolutePath,
                    "-S", templateRoot.absolutePath,
                )
            }
            exec {
                commandLine(
                    "cmake", "--build", hostPackDir.get().asFile.absolutePath,
                    "--target", "pack_archive", "-j",
                )
            }
        }
        val args = mutableListOf(
            packExe.get().asFile.absolutePath,
            "lua",
            file("$templateRoot/scripts").absolutePath,
            luaOut.get().asFile.absolutePath,
        )
        if (nxsConfig.exists()) {
            args.add(nxsConfig.absolutePath)
        }
        commandLine(args)
    }
}

tasks.register<Exec>("zigBuildRelease") {
    group = "nexus"
    description = "Build {{projectName}}.so via Zig for all Android ABIs"
    workingDir = file("$templateRoot/zig-services")
    val jniLibsDir = layout.buildDirectory.dir("jniLibs")

    outputs.dir(jniLibsDir)

    doLast {
        val abis = listOf(
            "aarch64-linux-android" to "arm64-v8a",
            "x86_64-linux-android" to "x86_64",
        )
        for ((targetTriple, abiDir) in abis) {
            val zigOut = layout.buildDirectory.dir("zig-out/$abiDir")
            exec {
                commandLine(
                    "zig", "build",
                    "-Dtarget=$targetTriple",
                    "-Doptimize=ReleaseSafe",
                    "--prefix", zigOut.get().asFile.absolutePath,
                )
            }
            val soFile = zigOut.get().asFile.resolve("lib/${project.name}.so")
            if (soFile.exists()) {
                val dest = jniLibsDir.get().asFile.resolve(abiDir)
                dest.mkdirs()
                soFile.copyTo(dest.resolve("${project.name}.so"), overwrite = true)
            }
        }
    }
}

// When using Zig backend, replace CMake externalNativeBuild with Zig .so
// To enable: uncomment the below and comment out externalNativeBuild { cmake {...} }
// android.defaultConfig.ndk.abiFilters = listOf("arm64-v8a", "x86_64")
// tasks.named("preBuild") { dependsOn("zigBuildRelease") }
// android.sourceSets.getByName("main") { jniLibs.srcDirs(layout.buildDirectory.dir("jniLibs")) }

tasks.named("preBuild") {
    dependsOn("copyBlueprintAsset", "packLuaDat")
}
