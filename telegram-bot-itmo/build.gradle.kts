plugins {
    kotlin("jvm") version "1.7.20"
    id("io.freefair.lombok") version "8.1.0"
}

group = "ru.vladrus13.itmobot"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("org.telegram:telegrambots:5.2.0")

    implementation(project("plugins:alarm"))
    implementation(project("plugins:simple"))
    implementation(project("plugins:homework"))
    implementation(project("skeleton"))

    implementation(libs.bundles.logging)
    implementation(libs.bundles.di)
}

tasks.withType<JavaExec> {
    jvmArgs = listOf("-Xms492m", "-Xmx492m")
}

apply {
    plugin("application")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs = listOf("-Xmx492m", "-Xmы492m")
}

configure<ApplicationPluginConvention> {
    mainClassName = "ru.vladrus13.itmobot.LauncherRun"
}

rec(project, buildDir)

fun rec(project: Project, buildDir: File) {
    project.buildDir = buildDir
    project.childProjects.forEach {
        rec(it.value, buildDir.resolve(it.key))
    }
}
