package ru.vladrus13.itmobot.plugin.practice.parsers.neerc

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.vladrus13.itmobot.plugin.practice.parsers.ChangeDifference
import ru.vladrus13.itmobot.plugin.practice.parsers.ParserInfo
import ru.vladrus13.itmobot.properties.InitialProperties
import java.io.IOException
import java.util.logging.Logger

class NeercParserInfo(override val idTable: String, override val urlInfo: String) : ParserInfo {
    val listTasks: MutableList<List<Int>> = mutableListOf()
    private val logger: Logger = InitialProperties.logger

    override suspend fun isChanged(): Boolean {
        val doc: Document

        try {
            doc = Jsoup.connect(urlInfo).timeout(10 * 1000).get()
        } catch (ignored: IOException) {
            return false
        }

        try {
            val classRoot = doc.getElementsByClass("mw-parser-output")[0]
            val countTasks = classRoot.getElementsByTag("ol")[0].childrenSize()


        } catch (e: IndexOutOfBoundsException) {
            logger.warning("Can't parse site \n${e.message}")
            return false
        }

        return false
    }

    override suspend fun getDifference(): ChangeDifference {
        TODO("Not yet implemented")
    }
}