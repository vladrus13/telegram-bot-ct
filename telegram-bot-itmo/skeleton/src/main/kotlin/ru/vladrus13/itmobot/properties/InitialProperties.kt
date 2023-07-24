package ru.vladrus13.itmobot.properties

import org.apache.logging.log4j.kotlin.Logging
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import java.io.IOException
import java.nio.file.Path
import java.util.*

class InitialProperties : Logging {
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

        private var nullableBot: TelegramLongPollingBot? = null

        var bot: TelegramLongPollingBot
            get() {
                check(nullableBot != null) { "Bot must be initialize" }
                return nullableBot!!
            }
            set(value) {
                nullableBot = value
            }

        const val timeToReloadTable = 5 * 60 * 1000L
    }
}