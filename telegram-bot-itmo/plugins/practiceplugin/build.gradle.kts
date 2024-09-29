plugins {
    kotlin("jvm")
}

group = "ru.vladrus13.itmobot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")
    // https://mvnrepository.com/artifact/org.jsoup/jsoup
    implementation("org.jsoup:jsoup:1.15.4")

    implementation("org.jetbrains.exposed:exposed:0.17.13")

    implementation("org.telegram:telegrambots:5.2.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.23.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4")

    // For using service account
    // https://mvnrepository.com/artifact/com.google.auth/google-auth-library-oauth2-http
    implementation("com.google.auth:google-auth-library-oauth2-http:1.22.0")

    // Dependencies
    // https://mvnrepository.com/artifact/com.google.api-client/google-api-client
    implementation("com.google.api-client:google-api-client:1.34.0")
    // https://mvnrepository.com/artifact/com.google.apis/google-api-services-drive
    implementation("com.google.apis:google-api-services-drive:v3-rev197-1.25.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev612-1.25.0")
    implementation(project(":skeleton"))

    // Other dependencies.
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
