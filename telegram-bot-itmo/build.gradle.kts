import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "ru.vladrus13.itmobot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.telegram:telegrambots:5.2.0")
    implementation("org.jetbrains.exposed:exposed:0.17.13")
    implementation("mysql:mysql-connector-java:8.0.25")
    implementation("com.google.api-client:google-api-client:1.31.5")
    implementation("com.google.oauth-client:google-oauth-client:1.31.5")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev110-1.23.0")
    implementation("org.jsoup:jsoup:1.14.1")

    implementation("com.google.apis:google-api-services-sheets:v4-rev614-1.18.0-rc")

    implementation(files("lib/apiMaster-1.0.jar"))
    implementation(files("lib/shikimoriAPI-1.0-SNAPSHOT.jar"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "13"
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
