plugins {
    kotlin("jvm")
}

group = "ru.vladrus13.itmobot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.exposed:exposed:0.17.13")
    implementation("org.telegram:telegrambots:5.2.0")

    implementation(project(":skeleton"))
}