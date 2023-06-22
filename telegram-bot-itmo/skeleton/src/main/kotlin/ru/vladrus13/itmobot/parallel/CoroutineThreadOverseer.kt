package ru.vladrus13.itmobot.parallel

import kotlinx.coroutines.Job

class CoroutineThreadOverseer {
    companion object {
        private val jobs: MutableList<Job> = mutableListOf()

        fun addTask(job: Job) {
            jobs.add(job)
        }

        suspend fun runTasks() {
            for (item in jobs) {
                item.start()
            }
            for (item in jobs) {
                item.join()
            }
        }
    }
}