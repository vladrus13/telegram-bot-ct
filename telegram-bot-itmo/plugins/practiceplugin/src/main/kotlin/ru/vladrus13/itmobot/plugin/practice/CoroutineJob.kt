package ru.vladrus13.itmobot.plugin.practice

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import ru.vladrus13.itmobot.database.DataBaseParser
import ru.vladrus13.itmobot.google.GoogleTableResponse
import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleSheet
import ru.vladrus13.itmobot.plugin.practice.parsers.neerc.NeercParserInfo
import ru.vladrus13.itmobot.plugin.practice.transfer.TransferData.Companion.transferFCSToLastName
import ru.vladrus13.itmobot.plugin.practice.transfer.TransferData.Companion.transferStudentTableToTeacher
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.utils.Messager
import java.lang.Thread.sleep
import java.util.Objects.deepEquals
import java.util.logging.Logger

class CoroutineJob {
    companion object {
        private val logger: Logger = InitialProperties.logger

        const val NEERC_JOB = 1L

        fun addTask(id: Long, jobId: Long, sourceLink: String, tableLink: String, tableId: String, userId: Long) {
            transaction(DataBaseParser.connection) {
                SheetJobTable.insert {
                    it[SheetJobTable.id] = id
                    it[SheetJobTable.jobId] = jobId
                    it[SheetJobTable.sourceLink] = sourceLink
                    it[SheetJobTable.tableLink] = tableLink
                    it[SheetJobTable.tableId] = tableId
                    it[chatId] = userId
                }
            }
        }

        fun runTasks(sendMessage: (String) -> Unit, prefix: String? = null): Boolean {
            var allSheetTables: List<ResultRow> = emptyList()
            transaction(DataBaseParser.connection) {
                allSheetTables = SheetJobTable
                    // 3239 -> 32 && 32** -> 32
                    .select {
                        if (prefix == null) return@select Op.TRUE

                        SheetJobTable.id / 100 eq prefix.take(2).toLong()
                    }
                    .toList()
                    .sortedBy { it[SheetJobTable.id].toInt() }
            }
            var result = true

            for (rw in allSheetTables) {
                if (!runTask(rw)) {
                    result = false
                    sendMessage("Бот не смог обновить таблицу группы ${rw[SheetJobTable.id]}")
                } else {
                    sendMessage("Бот смог обновить таблицу группы ${rw[SheetJobTable.id]}")
                }
            }
            return result
        }

        fun runTask(name: String): Boolean {
            var sheetTable: List<ResultRow> = emptyList()
            transaction(DataBaseParser.connection) {
                sheetTable = SheetJobTable
                    .select { SheetJobTable.id eq name.toLong() }
                    .toList()
            }
            if (sheetTable.size != 1) {
                return false
            }

            return runTask(sheetTable[0])
        }

        private fun runTask(row: ResultRow): Boolean {
            logger.info("Running task ${row[SheetJobTable.id]}")

            for (i in 1..RETRY_COUNT) {
                val id = row[SheetJobTable.id]
                val jobId = row[SheetJobTable.jobId]
                val sourceLink = row[SheetJobTable.sourceLink]
                val tableLink = row[SheetJobTable.tableLink]
                val tableId = row[SheetJobTable.tableId]
                val chatId = row[SheetJobTable.chatId]
                try {
                    when (jobId) {
                        NEERC_JOB -> runNeercTask(id, sourceLink, tableId, tableLink, chatId)
                    }
                    return true
                } catch (e: Exception) {
                    val errorMessage =
                        "Unknown exception with table $id!\nLink: $tableLink!\nError:\n${e.stackTraceToString()}"
                    logger.severe(errorMessage)
                    Messager.sendMessage(bot = InitialProperties.bot, chatId = chatId, text = errorMessage)
                    if (i < RETRY_COUNT) sleep(60 * 1000)
                }
            }
            logger.info("Refreshing table job stopped after $RETRY_COUNT attempts")
            return false
        }

        @Throws(Exception::class)
        private fun runNeercTask(groupId: Long, sourceLink: String, tableId: String, tableLink: String, chatId: Long) {
            val actualTasks: List<String> = NeercParserInfo(sourceLink).getTasks()
            val googleSheet = GoogleSheet(GoogleTableResponse.createSheetsService(), tableId)

            val fcsTasksWithMarks = googleSheet.getFCSTasksWithMarks()
            val currentTasks: List<String> = fcsTasksWithMarks.firstOrNull()?.drop(1) ?: listOf()
            val teacherSheetBody: List<List<String>> =
                if (fcsTasksWithMarks.isEmpty()) listOf()
                else fcsTasksWithMarks
                    .transferStudentTableToTeacher(chatId)
                    .transferFCSToLastName()
                    .plus(List(AMOUNT_CLEAR_ROW_AFTER) { listOf("") })

            logger.info("Sleep for 10 seconds")
            sleep(10 * 1000)
            logger.info("End sleep")

            // Add newList
            if (currentTasks.isEmpty() && actualTasks.isNotEmpty() || actualTasks.last() != currentTasks.last()) {
                googleSheet.generateSheet(actualTasks.subList(currentTasks.size, actualTasks.size))
            }

            // Update teacher sheet List
            if (teacherSheetBody.isNotEmpty() && !deepEquals(googleSheet.getTeacherList(), teacherSheetBody)) {
                googleSheet.updateFields(teacherSheetBody)
            }
            logger.info("End with group $groupId, link $tableLink")
        }

        private const val RETRY_COUNT = 2
        private const val AMOUNT_CLEAR_ROW_AFTER = 300
    }
}
