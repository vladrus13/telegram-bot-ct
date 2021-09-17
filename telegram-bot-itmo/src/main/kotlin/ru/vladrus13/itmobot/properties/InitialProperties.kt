package ru.vladrus13.itmobot.properties

import java.io.IOException
import java.nio.file.Path
import java.util.*

class InitialProperties {
    companion object {
        val mainProperties: Properties
            get() {
                val properties = Properties()
                try {
                    properties.load(InitialProperties::class.java.getResourceAsStream("/main.properties"))
                } catch (e: IOException) {
                    throw NoSuchFileException(Path.of("src/main/resources/main.properties").toFile())
                } catch (e: NullPointerException) {
                    throw NoSuchFileException(Path.of("src/main/resources/main.properties").toFile())
                }
                return properties
            }

        val databaseProperties: Properties
            get() {
                val properties = Properties()
                try {
                    properties.load(InitialProperties::class.java.getResourceAsStream("/datatable.properties"))
                } catch (e: IOException) {
                    throw NoSuchFileException(Path.of("src/main/resources/datatable.properties").toFile())
                } catch (e: NullPointerException) {
                    throw NoSuchFileException(Path.of("src/main/resources/datatable.properties").toFile())
                }
                return properties
            }
    }
}