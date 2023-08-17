package ru.vladrus13.itmobot.plugin.practice.parsers.neerc

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.vladrus13.itmobot.plugin.practice.parsers.ParserInfo
import ru.vladrus13.itmobot.properties.InitialProperties
import java.io.IOException
import java.util.logging.Logger

class NeercParserInfo(override val idTable: String, override val urlInfo: String) : ParserInfo {
    private val logger: Logger = InitialProperties.logger

    override fun getTasks(): List<String> {
        logger.info("== Starts parse data in neerc")

        val doc: Document
        try {
            logger.info("=== Launch get query for getting neerc html")
            doc = Jsoup.connect(urlInfo).timeout(10 * 1000).get()
            logger.info("=== Got neerc html")
        } catch (ignored: IOException) {
            logger.warning("=== Downloading html was too long...")
            return listOf()
        }

        try {
            logger.info("=== Starts counting tasks")
            val classRoot = doc.getElementsByClass("mw-parser-output")[0]
            val countTasks = classRoot.getElementsByTag("ol")[0].childrenSize()
            logger.info("=== Count of tasks: $countTasks")
            return (1..countTasks).map(Int::toString)
        } catch (e: IndexOutOfBoundsException) {
            logger.warning("Can't parse site \n${e.message}")
            return listOf()
        }
    }
}