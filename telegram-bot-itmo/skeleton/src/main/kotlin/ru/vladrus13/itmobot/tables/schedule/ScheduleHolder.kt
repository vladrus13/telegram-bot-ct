package ru.vladrus13.itmobot.tables.schedule

import org.jsoup.Jsoup
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.utils.TableUtils
import java.util.*


class ScheduleHolder {
    companion object {
        private val link: String = InitialProperties.mainProperties.getProperty("SCHEDULE_LINK")

        private const val timeoutTableGet = 1000 * 20
        var table = ScheduleTable.Table(Date(0))

        fun reload() {
            val full = Jsoup.connect(link).timeout(timeoutTableGet).get().getElementsByTag("table")[0].getElementsByTag(
                "tbody"
            )[0]
            try {
                table = ScheduleTable.Table(TableUtils.getTableFromHTML(full), Date())
            } catch (e : Exception) {
                throw Exception("Exception while parsing schedule table", e)
            }
        }
    }
}