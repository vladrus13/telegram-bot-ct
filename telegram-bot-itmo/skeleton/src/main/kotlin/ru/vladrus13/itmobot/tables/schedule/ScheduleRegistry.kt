package ru.vladrus13.itmobot.tables.schedule

import com.google.inject.Singleton
import org.apache.logging.log4j.kotlin.Logging
import org.jsoup.Jsoup
import ru.vladrus13.itmobot.parallel.ThreadHolder
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.utils.SafeRunnable
import ru.vladrus13.itmobot.utils.TableUtils
import java.util.*
import java.util.concurrent.TimeUnit


@Singleton
class ScheduleRegistry : Logging {
    private val link: String = InitialProperties.mainProperties.getProperty("SCHEDULE_LINK")

    private val timeoutTableGet = InitialProperties.timeToReloadScheduleTable
    var table = ScheduleTable.Table(Date(0))

    init {
        reload()
        ThreadHolder.scheduledExecutorService.scheduleAtFixedRate(SafeRunnable(::reload),
            timeoutTableGet, timeoutTableGet, TimeUnit.MILLISECONDS)
    }

    fun reload() {
        val startMillis = System.currentTimeMillis()
        logger.info("=== Schedule reload start")
        val full = Jsoup.connect(link)
            .timeout(timeoutTableGet.toInt())
            .get()
            .getElementsByTag("table")[0]
            .getElementsByTag("tbody")[0]
        table = ScheduleTable.Table(TableUtils.getTableFromHTML(full), Date())
        logger.info("=== Schedule reload finished. Time = ${System.currentTimeMillis() - startMillis} ms")
    }
}