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
    implementation("com.google.api-client:google-api-client:1.31.5")
    implementation("com.google.oauth-client:google-oauth-client:1.31.5")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev110-1.23.0")

    implementation("com.google.apis:google-api-services-sheets:v4-rev614-1.18.0-rc")

    implementation(project(":skeleton"))
}