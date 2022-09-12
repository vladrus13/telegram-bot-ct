package ru.vladrus13.itmobot.tables

import ru.vladrus13.itmobot.parallel.ThreadHolder
import ru.vladrus13.itmobot.properties.InitialProperties.Companion.logger
import ru.vladrus13.itmobot.properties.InitialProperties.Companion.timeToReloadTable
import ru.vladrus13.itmobot.tables.schedule.ScheduleHolder
import ru.vladrus13.itmobot.utils.Writer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

class TableGroupsHolder {
    companion object {
        private val map: ConcurrentHashMap<String, Table> = ConcurrentHashMap()
        val changes: ConcurrentHashMap<String, ConcurrentLinkedDeque<ResultPair>> = ConcurrentHashMap()
        private const val timeToChange: Long = timeToReloadTable

        var isReady = false

        fun set(table: Table) {
            map[table.name] = table
        }

        operator fun get(name: String): Table? {
            return map[name]
        }

        private fun merger(
            a: ConcurrentLinkedDeque<ResultPair>,
            b: ConcurrentLinkedDeque<ResultPair>
        ): ConcurrentLinkedDeque<ResultPair> {
            a.addAll(b)
            return a
        }

        fun run() {
            ThreadHolder.executorService.submit {
                try {
                    while (true) {
                        var current = Date()
                        logger.info("=== Main table reload start")
                        try {
                            MainTableHolder.reload()
                        } catch (e: Exception) {
                            Writer.printStackTrace(logger, e)
                        }
                        logger.info("=== Main table reload finished. Time = ${Date().time - current.time} ms")
                        current = Date()
                        logger.info("=== Schedule reload start")
                        try {
                            ScheduleHolder.reload()
                        } catch (e: Exception) {
                            Writer.printStackTrace(logger, e)
                        }
                        logger.info("=== Schedule reload finished. Time = ${Date().time - current.time} ms")
                        for (table in map.elements()) {
                            val timer = Date()
                            logger.info("=== Table \"${table.name}\" start check")
                            if (current.after(table.nextUpdate)) {
                                logger.info("==== ${table.name} table reload")
                                changes.merge(
                                    table.name,
                                    ConcurrentLinkedDeque(table.reload())
                                ) { old, new -> merger(old, new) }
                            }
                            logger.info("=== Table \"${table.name}\" finish check. Time = ${Date().time - timer.time} ms")
                        }
                        isReady = true
                        Thread.sleep(timeToChange)
                    }
                } catch (e: Exception) {
                    Writer.printStackTrace(logger = logger, e)
                }
            }
        }
    }
}