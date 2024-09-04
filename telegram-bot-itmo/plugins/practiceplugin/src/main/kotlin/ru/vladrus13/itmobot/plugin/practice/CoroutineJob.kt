package ru.vladrus13.itmobot.plugin.practice

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
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

        fun runTasks(): Boolean {
            var allSheetTables: List<ResultRow> = emptyList()
            transaction(DataBaseParser.connection) {
                allSheetTables = SheetJobTable
                    .selectAll()
                    .toList()
                    .sortedBy { it[SheetJobTable.id].toInt() }
            }
            for (rw in allSheetTables) {
                if (!runTask(rw)) {
                    return false
                }
            }
            return true
        }

        private fun runTask(row: ResultRow): Boolean {
            for (i in 1..RETRY_COUNT) {
                val id = row[SheetJobTable.id]
                val jobId = row[SheetJobTable.jobId]
                val sourceLink = row[SheetJobTable.sourceLink]
                val tableLink = row[SheetJobTable.tableLink]
                val tableId = row[SheetJobTable.tableId]
                val chatId = row[SheetJobTable.chatId]
                try {
                    when (jobId) {
                        NEERC_JOB -> runNeercTask(id, sourceLink, tableId, tableLink)
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
        private fun runNeercTask(groupId: Long, sourceLink: String, tableId: String, tableLink: String) {
            val actualTasks: List<String> = NeercParserInfo(sourceLink).getTasks()
            val googleSheet = GoogleSheet(GoogleTableResponse.createSheetsService(), tableId)

            val fcsTasksWithMarks = googleSheet.getFCSTasksWithMarks()
            val currentTasks: List<String> = fcsTasksWithMarks.firstOrNull()?.drop(1) ?: listOf()
            val teacherSheetBody: List<List<String>> =
                if (fcsTasksWithMarks.isEmpty()) listOf()
                else fcsTasksWithMarks
                    .transferStudentTableToTeacher()
                    .transferFCSToLastName()

            logger.fine("Sleep for 60 seconds")
            sleep(60 * 1000)
            logger.fine("End sleep for 60 seconds")

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

        private const val RETRY_COUNT = 5
    }
}
