package ru.vladrus13.itmobot.plugin.practice

import com.google.api.client.auth.oauth2.TokenResponseException
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
import java.io.IOException
import java.lang.Thread.sleep
import java.util.Objects.deepEquals
import java.util.logging.Logger

class CoroutineJob {
    companion object {
        private val logger: Logger = InitialProperties.logger

        const val NEERC_JOB = 1L

        fun addTask(jobId: Long, sourceLink: String, tableLink: String, tableId: String, userId: Long) {
            transaction(DataBaseParser.connection) {
                SheetJobTable.insert {
                    it[SheetJobTable.jobId] = jobId
                    it[SheetJobTable.sourceLink] = sourceLink
                    it[SheetJobTable.tableLink] = tableLink
                    it[SheetJobTable.tableId] = tableId
                    it[SheetJobTable.userId] = userId
                }
            }
        }

        fun runTasks(tableIndex: Int, batchSize: Int = 1): Int {
            var nextIndex: Int = 0
            transaction(DataBaseParser.connection) {
                val allSheetTables = SheetJobTable.selectAll().toList().sortedBy { it[SheetJobTable.id] }
                if (allSheetTables.isNotEmpty()) {
                    nextIndex = (tableIndex + batchSize) % allSheetTables.size
                    allSheetTables
                        .drop(tableIndex)
                        .take(batchSize)
                        .forEach(CoroutineJob::runTask)
                }
            }
            return nextIndex
        }

        private fun runTask(row: ResultRow) {
            while (true) {
                try {
                    val jobId = row[SheetJobTable.jobId]
                    val sourceLink = row[SheetJobTable.sourceLink]
                    val tableLink = row[SheetJobTable.tableLink]
                    val tableId = row[SheetJobTable.tableId]
                    when (jobId) {
                        NEERC_JOB -> runNeercTask(sourceLink, tableId, tableLink)
                    }
                    break;
                } catch (e: IOException) {
                    logger.warning("IOException: " + e.stackTraceToString())
                } catch (e: TokenResponseException) {
                    logger.warning("Something wrong! Check situation with google API: " + e.stackTraceToString())
                }
            }
        }

        @Throws(IOException::class, TokenResponseException::class)
        private fun runNeercTask(sourceLink: String, tableId: String, tableLink: String) {
            val actualTasks: List<String> = NeercParserInfo(sourceLink).getTasks()
            val googleSheet = GoogleSheet(GoogleTableResponse.createSheetsService(), tableId)

            logger.info("Indicator of creating new teacher sheet: ${googleSheet.generateTeacherSheet()}")

            val fcsTasksWithMarks = googleSheet.getFCSTasksWithMarks()
            val currentTasks: List<String> = fcsTasksWithMarks.firstOrNull()?.drop(1) ?: listOf()
            val teacherSheetBody: List<List<String>> =
                if (fcsTasksWithMarks.isEmpty()) listOf()
                else fcsTasksWithMarks.transferStudentTableToTeacher().transferFCSToLastName()

            logger.info("Sleep for 60 seconds")
            sleep(60 * 1000)
            logger.info("End sleep for 60 seconds")

            // Add newList
            if (currentTasks.isEmpty() && actualTasks.isNotEmpty() || actualTasks.last() != currentTasks.last()) {
                googleSheet.generateSheet(actualTasks.subList(currentTasks.size, actualTasks.size))
            }

            // Update teacher sheet List
            if (teacherSheetBody.isNotEmpty() && !deepEquals(googleSheet.getTeacherList(), teacherSheetBody)) {
                googleSheet.updateFields(teacherSheetBody)
            }
            logger.info("End with this link $tableLink")
        }
    }
}
