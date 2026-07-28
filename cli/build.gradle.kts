plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

kotlin {
    jvmToolchain(26)
}

application {
    mainClass.set("com.nexus.framework.cli.FrameworkCliKt")
}

dependencies {
    implementation(project(":core"))
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

tasks.register<JavaExec>("runCli") {
    group = "application"
    description = "Run the Framework CLI with --args=\"…\""
    workingDir = rootProject.projectDir
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.nexus.framework.cli.FrameworkCliKt")
    if (project.hasProperty("args")) {
        args = (project.property("args") as String).split(" ").filter { it.isNotEmpty() }
    }
}
