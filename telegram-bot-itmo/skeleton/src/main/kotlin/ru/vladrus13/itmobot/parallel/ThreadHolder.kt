package ru.vladrus13.itmobot.parallel

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ThreadHolder {
    companion object {
        val executorService: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    }
}