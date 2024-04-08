package ru.vladrus13.itmobot.google

import com.google.api.client.auth.oauth2.TokenResponseException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import ru.vladrus13.itmobot.properties.InitialProperties
import java.io.IOException
import java.lang.Thread.sleep
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.cancellation.CancellationException

class ExecuteSchedulerService {
    companion object {
        private const val N_THREADS = 1
        private val logger = InitialProperties.logger
        private val lock = ReentrantLock()
        private val executor = newFixedThreadPoolContext(N_THREADS, "Executor")

        @Volatile
        private var usedReadOperations: Int = 0

        @Volatile
        private var usedWriteOperations: Int = 0
        private const val FREE_PERIOD: Long = 60 * 1000
        private const val SECONDS_LIMIT: Int = 60 * 1000
        private const val ALL_OPERATIONS_LIMIT = 300
        private const val CURRENT_OPERATIONS_LIMIT = 240

        init {
            GlobalScope.launch {
                while (true) {
                    sleep(FREE_PERIOD)
                    lock.lock()
                    try {
                        logger.info("Update read&write operations to zero")
                        usedReadOperations = 0
                        usedWriteOperations = 0
                    } finally {
                        lock.unlock()
                    }
                }
            }
        }

        private suspend fun tryToLaunch(readOp: Int, writeOp: Int) {
            var unsuccessful = true
            for (i in 1..SECONDS_LIMIT) {
                val checker: Boolean
                lock.lock()
                try {
                    checker = (readOp + usedReadOperations < CURRENT_OPERATIONS_LIMIT)
                            && (writeOp + usedWriteOperations < CURRENT_OPERATIONS_LIMIT)
                    if (checker) {
                        logger.info("Add to values new readOp&writeOp $readOp&$writeOp")
                        usedReadOperations += readOp
                        usedWriteOperations += writeOp
                    }
                } finally {
                    lock.unlock()
                }
                if (checker) {
                    unsuccessful = false

                    break
                }
                delay(1 * 1000)
            }
            if (unsuccessful) throw CancellationException()
        }

        suspend fun getValueRange(range: String, service: Sheets, id: String): ValueRange {
            tryToLaunch(1, 0)
            val res = launch {
                return@launch service
                    .spreadsheets()
                    .values()
                    .get(id, range)
                    .execute()
            }

            return res.get()
        }

        suspend fun executeRequestsSequence(
            service: Sheets,
            id: String,
            vararg requests: Request
        ) {
            tryToLaunch(0, requests.size)
            val res = launch {
                service
                    .spreadsheets()
                    .batchUpdate(id, BatchUpdateSpreadsheetRequest().setRequests(requests.toList()))
                    .execute()
                return@launch
            }

            res.get()
        }

        suspend fun createSpreadsheet(sheetService: Sheets, spreadsheet: Spreadsheet): Spreadsheet {
            tryToLaunch(0, 1)
            val res = launch {
                return@launch sheetService
                    .spreadsheets()
                    .create(spreadsheet)
                    .execute()
            }

            return res.get()
        }

        suspend fun getSpreadSheetById(service: Sheets, id: String): Spreadsheet {
            tryToLaunch(1, 0)
            val res = launch {
                return@launch service.spreadsheets().get(id).execute()
            }

            return res.get()
        }

        @Suppress("UNCHECKED_CAST")
        suspend fun getSheets(service: Sheets, address: String): ArrayList<Sheet> {
            tryToLaunch(1, 0)
            val res = launch {
                return@launch service.Spreadsheets().get(address).execute()["sheets"] as ArrayList<Sheet>
            }

            return res.get()
        }

        private suspend fun <T> launch(body: suspend () -> T): CompletableFuture<T> {
            val deferred = CompletableFuture<T>()
            executor.run {
                val result = body()
                deferred.complete(result)
            }
            return deferred
        }

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