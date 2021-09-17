package ru.vladrus13.itmobot.parallel

import java.util.concurrent.Executors

class ThreadHolder {
    companion object {
        val executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    }
}