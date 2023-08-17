package ru.vladrus13.itmobot.parallel

class CoroutineThreadOverseer {
    companion object {
        private val jobs: MutableList<() -> Unit> = mutableListOf()

        fun addTask(job: () -> Unit) {
            jobs.add(job)
        }

        fun runTasks() {
            for (action in jobs) {
                action()
            }
        }
    }
}