package ru.vladrus13.itmobot.tables

import org.apache.logging.log4j.kotlin.Logging
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.parallel.ThreadHolder
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.properties.InitialProperties.Companion.timeToReloadPointsTable
import java.util.concurrent.TimeUnit

class PointTablesReloader(private val pointTablesRegistry: PointTablesRegistry) : Logging {

    init {
        doRun()
        ThreadHolder.scheduledExecutorService.scheduleAtFixedRate({
            try {
                doRun()
            } catch (e: Exception) {
                logger.error("Something went wrong", e)
            }
        }, timeToReloadPointsTable, timeToReloadPointsTable, TimeUnit.MILLISECONDS)
    }

    private fun doRun() {
        val pointTables = pointTablesRegistry.pointsTablesByName.elements()
        for (table in pointTables) {
            val startReloadMillis = System.currentTimeMillis()
            logger.info("=== Table \"${table.name}\" start reload")
            val tableChanges = table.reload()
            logger.info(
                "=== Table \"${table.name}\" finish check. " +
                        "Time = ${System.currentTimeMillis() - startReloadMillis} ms"
            )
            sendUpdates(tableChanges, table.name)
        }
    }

    private fun sendUpdates(
        changes: ArrayList<ResultPair>,
        tableName: String
    ) {
        if (changes.isEmpty()) {
            logger.info("=== Table \"$tableName\" has no updates.")
            return
        }
        logger.info("=== Table \"$tableName\" starting send updates.")
        val startSendingUpdatesMillis = System.currentTimeMillis()
        val users = DataBase.getUsersWithNotification()
        for (singleChange in changes) {
            val user = users.find {
                it.name != null && it.group != null
                        && pointTablesRegistry[tableName]!!.isDepends(it.group!!)
                        && singleChange.name.contains(it.name!!)
            }
            if (user != null) {
                user.send(
                    bot = InitialProperties.bot,
                    text = "Произошло изменение в баллах!\n$tableName: ${singleChange.callChangeToString()}"
                )
            }
        }

        logger.info(
            "=== Table \"$tableName\" finished sending updates. " +
                    "Time = ${System.currentTimeMillis() - startSendingUpdatesMillis} ms"
        )
    }
}