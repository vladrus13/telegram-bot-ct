package ru.vladrus13.itmobot.plugin.practice.parsers.neerc

import org.jsoup.Jsoup
import ru.vladrus13.itmobot.plugin.practice.parsers.ChangeDifference
import ru.vladrus13.itmobot.plugin.practice.parsers.ParserInfo
import ru.vladrus13.itmobot.properties.InitialProperties
import java.util.logging.Logger

class NeercParserInfo(override val idTable: String, override val urlInfo: String) : ParserInfo {
    val listTasks: MutableList<List<Int>> = mutableListOf()
    private val logger: Logger = InitialProperties.logger

    override suspend fun isChanged(): Boolean {
        try {
            val doc = Jsoup.connect(urlInfo).get()
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