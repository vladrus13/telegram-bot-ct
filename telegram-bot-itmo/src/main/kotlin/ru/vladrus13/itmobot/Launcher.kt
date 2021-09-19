@file:JvmName("LauncherRun")

package ru.vladrus13.itmobot

import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import org.xml.sax.SAXException
import ru.vladrus13.itmobot.Launcher.Companion.bot
import ru.vladrus13.itmobot.bot.ItmoBot
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.exceptions.XMLClassCastException
import ru.vladrus13.itmobot.parallel.TableChangesHolder
import ru.vladrus13.itmobot.plugins.PluginsHolder
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.tables.TableGroupsHolder
import ru.vladrus13.itmobot.utils.Writer
import ru.vladrus13.itmobot.xml.XMLParser
import java.util.*
import java.util.logging.LogManager
import java.util.logging.Logger

class Launcher {
    companion object {
        val logger: Logger = Logger.getLogger(Launcher::class.java.simpleName)
        val bot = ItmoBot()
    }
}

fun main() {
    try {
        LogManager.getLogManager()
            .readConfiguration(Launcher::class.java.getResourceAsStream("/logging_main.properties"))
        LogManager.getLogManager()
            .readConfiguration(DataBase::class.java.getResourceAsStream("/logging_datatable.properties"))
    } catch (e: NullPointerException) {
        println("Can't load logging properties. Stop bot")
        return
    }
    InitialProperties.logger = Launcher.logger
    InitialProperties.bot = bot
    Launcher.logger.info("= Launch bot")
    Launcher.logger.info("== Start timezone default")
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+3"))
    Launcher.logger.info("== Finish timezone default")
    Launcher.logger.info("== Start loading properties")
    try {
        Launcher.logger.info("=== Load main properties file")
        InitialProperties.mainProperties
        Launcher.logger.info("=== Load datatable properties file")
        InitialProperties.databaseProperties
    } catch (e: NoSuchFileException) {
        Launcher.logger.severe("=== Failed at loading property file ${e.file.name}. File not found. Stop bot")
        return
    }
    Launcher.logger.info("== Finish loading properties")

    Launcher.logger.info("== Start loading XML")
    try {
        XMLParser.init()
    } catch (e: NoSuchFileException) {
        Launcher.logger.severe("=== Failed at loading XML file ${e.file.name}. File not found. Stop bot")
        return
    } catch (e: SAXException) {
        Launcher.logger.severe("=== Failed at parse XML file. Stop bot")
        return
    } catch (e: XMLClassCastException) {
        Launcher.logger.severe("=== Failed at parse XML file. Can't cast class ${e.found.qualifiedName} to class ${e.field}")
        return
    } catch (e: ClassNotFoundException) {
        Launcher.logger.severe("=== Failed at parse XML file. Can't find class")
        return
    } catch (e: NullPointerException) {
        Launcher.logger.severe("=== Failed at parse XML file. ${e.message}")
        return
    } catch (e: Exception) {
        Writer.printStackTrace(Launcher.logger, e)
        return
    }
    Launcher.logger.info("== Finish loading XML")

    Launcher.logger.info("== Start loading database")
    try {
        DataBase.init()
    } catch (e: IllegalArgumentException) {
        Writer.printStackTrace(Launcher.logger, e)
    }
    Launcher.logger.info("== Finish loading database")

    Launcher.logger.info("== Start initial plugins")
    PluginsHolder.init()
    Launcher.logger.info("== Finish initial plugins")

    Launcher.logger.info("== Start creating bot API")
    val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
    Launcher.logger.info("== Finish creating bot API")


    TableGroupsHolder.run()
    TableChangesHolder.run()
    try {
        telegramBotsApi.registerBot(bot)
    } catch (e: TelegramApiRequestException) {
        Writer.printStackTrace(Launcher.logger, e)
    }
}
