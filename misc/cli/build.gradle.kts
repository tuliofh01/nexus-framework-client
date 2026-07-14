plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
}

application {
    mainClass.set("nexus.opensource.framework.cli.FrameworkCliKt")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

dependencies {
    implementation(project(":core"))
}

tasks.register<JavaExec>("runCli") {
    group = "application"
    description = "Run the Framework CLI with --args=\"…\""
    workingDir = rootProject.projectDir
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("nexus.opensource.framework.cli.FrameworkCliKt")
    if (project.hasProperty("args")) {
        args = (project.property("args") as String).split(" ").filter { it.isNotEmpty() }
    }
}
