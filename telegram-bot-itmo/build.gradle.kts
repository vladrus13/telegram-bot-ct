import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
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

task<JavaExec>("runBot") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("ru.vladrus13.itmobot.LauncherRun")
}

tasks.create<ShadowJar>("runBotJar") {
    mergeServiceFiles()
    group = "shadow"
    description = "Run bot"

    from(sourceSets.main.get().output)
    from(project.configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    configurations = listOf(project.configurations.runtimeClasspath.get())

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("runBot.jar")
    manifest {
        attributes["Main-Class"] = (tasks.findByName("runBot") as JavaExec).mainClass.get()
    }
}.apply task@ { tasks.named("jar") { dependsOn(this@task) } }

tasks.test {
    useJUnitPlatform()
}
