package ru.vladrus13.itmobot.google

import com.google.api.client.auth.oauth2.TokenResponseException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import ru.vladrus13.itmobot.properties.InitialProperties
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class ExecuteSchedulerService {
    companion object {
        data class Operation(
            val function: () -> Unit,
            val counts: OperationCounts,
        )

        data class OperationCounts(
            var read: Int,
            var write: Int,
        ) {
            fun isOverflow(): Boolean =
                read < CURRENT_OPERATIONS_LIMIT && write < CURRENT_OPERATIONS_LIMIT

            fun setZero() {
                read = 0
                write = 0
            }

            fun addCounts(counts: OperationCounts) {
                read += counts.read
                write += counts.write
            }
        }

        private val logger = InitialProperties.logger
        private var isShouldWait = AtomicBoolean(false)
        private val doneOperationCounts = OperationCounts(0, 0)

        private const val FREE_PERIOD: Long = 60 * 1000
        private const val CURRENT_OPERATIONS_LIMIT = 240

        private val input = Channel<Operation>(100)
        private val executor = Channel<Operation>(100)

        private suspend fun <T> getValue(f: () -> T, counts: OperationCounts): T {
            val complete = CompletableDeferred<T>()
            input.send(
                Operation(
                    function = { complete.complete(f()) },
                    counts = counts,
                )
            )
            return complete.await()
        }

        private suspend fun waitSender() = coroutineScope {
            logger.info("start wait sender")
            try {
                while (isActive) {
                    delay(1000)
                    if (isShouldWait.get()) {
                        delay(FREE_PERIOD)
                        isShouldWait.set(false)
                    }
                    executor.send(input.receive())
                }
            } finally {
                logger.info("end wait sender")
            }
        }

        private suspend fun executor() = coroutineScope {
            logger.info("start executor")
            try {
                while (isActive) {
                    val op = executor.receive()
                    doneOperationCounts.addCounts(op.counts)

                    if (doneOperationCounts.isOverflow()) {
                        doneOperationCounts.setZero()
                        isShouldWait.set(true)
                        delay(FREE_PERIOD)
                        doneOperationCounts.addCounts(op.counts)
                    }

                    logger.info(doneOperationCounts.toString())
                    op.function()
                }
            } finally {
                logger.info("end executor")
            }
        }

        suspend fun init() = coroutineScope {
            launch { waitSender() }
            launch { executor() }
        }

        suspend fun getValueRange(range: String, service: Sheets, id: String): ValueRange = getValue(
            {
                service
                    .spreadsheets()
                    .values()
                    .get(id, range)
                    .execute()
            },
            OperationCounts(1, 0)
        )

        suspend fun executeRequestsSequence(service: Sheets, id: String, vararg requests: Request) = getValue(
            {
                service
                    .spreadsheets()
                    .batchUpdate(id, BatchUpdateSpreadsheetRequest().setRequests(requests.toList()))
                    .execute()
            },
            OperationCounts(0, requests.size)
        )

        suspend fun createSpreadsheet(sheetService: Sheets, spreadsheet: Spreadsheet): Spreadsheet = getValue(
            {
                sheetService
                    .spreadsheets()
                    .create(spreadsheet)
                    .execute()
            },
            OperationCounts(0, 1)
        )

        suspend fun getSpreadSheetById(service: Sheets, id: String): Spreadsheet = getValue(
            {
                service.spreadsheets().get(id).execute()
            },
            OperationCounts(1, 0)
        )

        @Suppress("UNCHECKED_CAST")
        suspend fun getSheets(service: Sheets, address: String): ArrayList<Sheet> = getValue(
            {
                service.Spreadsheets().get(address).execute()["sheets"] as ArrayList<Sheet>
            },
            OperationCounts(1, 0)
        )

        @Throws(IOException::class, TokenResponseException::class)
        fun insertPermission(
            service: Drive,
            fileId: String,
        ): Permission = service
            .Permissions()
            .create(fileId, Permission().setType("anyone").setRole("writer"))
            .execute()
    }
}
