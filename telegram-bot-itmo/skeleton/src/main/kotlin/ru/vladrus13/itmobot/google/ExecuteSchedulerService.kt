package ru.vladrus13.itmobot.google

import com.google.api.client.auth.oauth2.TokenResponseException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import kotlinx.coroutines.*
import ru.vladrus13.itmobot.properties.InitialProperties
import java.io.IOException
import java.lang.Thread.sleep
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.cancellation.CancellationException

class ExecuteSchedulerService {
    companion object {
        private val logger = InitialProperties.logger
        private val deque = ArrayDeque<Triple<Int, Int, Deferred<Any>>>()
        private val lock = ReentrantLock()

        @Volatile
        private var usedReadOperations: Int = 0

        @Volatile
        private var usedWriteOperations: Int = 0
        private const val FREE_PERIOD: Long = 60 * 1000
        private const val SECONDS_LIMIT: Int = 60 * 1000
        private const val ALL_OPERATIONS_LIMIT = 300
        private const val CURRENT_OPERATIONS_LIMIT = (ALL_OPERATIONS_LIMIT * 0.5).toInt()

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
            GlobalScope.launch {
                while (true) {
                    if (deque.isEmpty()) {
                        logger.info("Don't have any job")
                        sleep(1 * 1000)
                        continue
                    }
                    val inLimit: Boolean
                    lock.lock()
                    try {
                        inLimit = deque.first().first + usedReadOperations < CURRENT_OPERATIONS_LIMIT &&
                                deque.first().second + usedWriteOperations < CURRENT_OPERATIONS_LIMIT
                    } finally {
                        lock.unlock()
                    }
                    if (inLimit) {
                        val triple = deque.removeFirst()
                        triple.third.start()
                        lock.lock()
                        try {
                            usedReadOperations += triple.first
                            usedWriteOperations += triple.second
                            logger.info("Got a job, now: $usedReadOperations&$usedWriteOperations")
                        } finally {
                            lock.unlock()
                        }
                    } else {
                        sleep(1 * 1000)
                    }
                }
            }
        }

        private fun tryToLaunch(worker: Deferred<Any>) {
            var unsuccessful = true
            for (i in 1..SECONDS_LIMIT) {
                if (worker.isCompleted) {
                    unsuccessful = false
                    break
                }
                sleep(1 * 1000)
            }
            if (unsuccessful) throw CancellationException()
        }

        suspend fun getValueRange(range: String, service: Sheets, id: String): ValueRange {
            val worker = CoroutineScope(Job())
                .async(start = CoroutineStart.LAZY) {
                    return@async service
                        .spreadsheets()
                        .values()
                        .get(id, range)
                        .execute()
                }
            deque.addLast(Triple(1, 0, worker))
            tryToLaunch(worker)

            return worker.await()
        }


        fun executeRequestsSequence(
            service: Sheets,
            id: String,
            vararg requests: Request
        ) {
            val worker = CoroutineScope(Job()).async(start = CoroutineStart.LAZY) {
                service
                    .spreadsheets()
                    .batchUpdate(id, BatchUpdateSpreadsheetRequest().setRequests(requests.toList()))
                    .execute()
                return@async
            }
            deque.addLast(Triple(0, requests.size, worker))
            tryToLaunch(worker)
        }

        suspend fun createSpreadsheet(sheetService: Sheets, spreadsheet: Spreadsheet): Spreadsheet {
            val worker = CoroutineScope(Job()).async(start = CoroutineStart.LAZY) {
                return@async sheetService
                    .spreadsheets()
                    .create(spreadsheet)
                    .execute()
            }
            deque.addLast(Triple(0, 1, worker))
            tryToLaunch(worker)

            return worker.await()
        }

        suspend fun getSpreadSheetById(service: Sheets, id: String): Spreadsheet {
            val worker = CoroutineScope(Job()).async(start = CoroutineStart.LAZY) {
                return@async service.spreadsheets().get(id).execute()
            }
            deque.addLast(Triple(1, 0, worker))
            tryToLaunch(worker)

            return worker.await()
        }

        @Suppress("UNCHECKED_CAST")
        suspend fun getSheets(service: Sheets, address: String): ArrayList<Sheet> {
            val worker = CoroutineScope(Job()).async(start = CoroutineStart.LAZY) {
                return@async service.Spreadsheets().get(address).execute()["sheets"] as ArrayList<Sheet>
            }
            deque.addLast(Triple(1, 0, worker))
            tryToLaunch(worker)

            return worker.await()
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