plugins {
    id("com.android.application")
    id("com.chaquo.python")
    id("org.jetbrains.kotlin.android")
}

val templateRoot = rootProject.projectDir
val generatedAssets = layout.buildDirectory.dir("generated/nxs-assets")

android {
    namespace = "com.nexus.plotter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nexus.plotter"
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
            assets.srcDirs(
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

tasks.named("preBuild") {
    dependsOn("copyBlueprintAsset")
}
