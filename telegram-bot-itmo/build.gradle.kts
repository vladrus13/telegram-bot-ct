plugins {
    kotlin("jvm") version "1.9.21"
}

group = "ru.vladrus13.itmobot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")
    implementation("org.telegram:telegrambots:5.2.0")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.9") {
        because("We want to remove warning about slf4j")
    }
    implementation("org.slf4j:slf4j-simple:2.0.9") {
        because("We want to remove warning about slf4j")
    }

    implementation(project("plugins:alarm"))
    implementation(project("plugins:shipper"))
    implementation(project("plugins:simple"))
    implementation(project("plugins:homework"))
    implementation(project("plugins:practiceplugin"))
    implementation(project("skeleton"))

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
