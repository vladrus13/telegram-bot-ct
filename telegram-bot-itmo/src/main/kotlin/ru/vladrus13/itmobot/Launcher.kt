@file:JvmName("LauncherRun")

package ru.vladrus13.itmobot

import com.google.inject.Guice
import org.apache.logging.log4j.kotlin.logger
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import org.xml.sax.SAXException
import ru.vladrus13.itmobot.bot.BotModule
import ru.vladrus13.itmobot.bot.ItmoBot
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.exceptions.XMLClassCastException
import ru.vladrus13.itmobot.plugins.PluginsHolder
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.tables.TableModule
import ru.vladrus13.itmobot.xml.XMLParser
import java.util.*

fun main() {
    val logger = logger("main")
    logger.info("= Launch bot")
    logger.info("== Start timezone default")
    TimeZone.setDefault(TimeZone.getTimeZone("GMT+3"))
    logger.info("== Finish timezone default")
    logger.info("== Start loading properties")
    try {
        logger.info("=== Load main properties file")
        InitialProperties.mainProperties
        logger.info("=== Load datatable properties file")
        InitialProperties.databaseProperties
    } catch (e: NoSuchFileException) {
        logger.error("=== Failed at loading property file ${e.file.name}. File not found. Stop bot", e)
        return
    }
    logger.info("== Finish loading properties")

    logger.info("== Start loading XML")
    try {
        XMLParser.init()
    } catch (e: NoSuchFileException) {
        logger.error("=== Failed at loading XML file ${e.file.name}. File not found. Stop bot", e)
        return
    } catch (e: SAXException) {
        logger.error("=== Failed at parse XML file. Stop bot", e)
        return
    } catch (e: XMLClassCastException) {
        logger.error("=== Failed at parse XML file. Can't cast class ${e.found.qualifiedName} to class ${e.field}", e)
        return
    } catch (e: ClassNotFoundException) {
        logger.error("=== Failed at parse XML file. Can't find class", e)
        return
    } catch (e: NullPointerException) {
        logger.error("=== Failed at parse XML file. ${e.message}", e)
        return
    } catch (e: Exception) {
        logger.error("=== Something went wrong loading XML", e)
        return
    }
    logger.info("== Finish loading XML")

    logger.info("== Start loading database")
    try {
        DataBase.init()
    } catch (e: IllegalArgumentException) {
        logger.error("Something went wrong loading database", e)
    }
    logger.info("== Finish loading database")

    logger.info("== Start initial plugins")
    PluginsHolder.init()
    logger.info("== Finish initial plugins")

    logger.info("== Start creating bot API")
    val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
    logger.info("== Finish creating bot API")

    val injector = Guice.createInjector(
        TableModule(),
        BotModule()
    )
    InitialProperties.bot = injector.getInstance(ItmoBot::class.java)
    try {
        telegramBotsApi.registerBot(InitialProperties.bot)
    } catch (e: TelegramApiRequestException) {
        logger.error("Somethign went wrong initalizing bot", e)
    }
}
