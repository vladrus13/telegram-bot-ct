rootProject.name = "telegram-bot-itmo"
include("skeleton")
include("plugins:alarm")
include("plugins:shikimori")
include("plugins:simple")
include("plugins:homework")


dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://repository.apache.org/snapshots")
    }

    versionCatalogs {
        create("libs") {
            // logging
            library("log4j-kotlin","org.apache.logging.log4j:log4j-api-kotlin:1.3.0-SNAPSHOT")
            library("log4j-api","org.apache.logging.log4j:log4j-api:2.20.0")
            library("log4j-core", "org.apache.logging.log4j:log4j-core:2.20.0")
            bundle("logging", mutableListOf("log4j-kotlin","log4j-api","log4j-core"))

            library("guice", "com.google.inject:guice:7.0.0")
            bundle("di", mutableListOf("guice"))
        }
    }
}