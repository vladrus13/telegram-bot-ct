plugins {
    kotlin("jvm")
}

group = "ru.vladrus13.itmobot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.telegram:telegrambots:5.2.0")

    implementation(files("../../lib/shikimoriAPI-1.0-SNAPSHOT.jar"))

    implementation(project(":skeleton"))
}