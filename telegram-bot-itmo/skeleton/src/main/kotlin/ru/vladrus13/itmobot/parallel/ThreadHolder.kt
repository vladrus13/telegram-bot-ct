package ru.vladrus13.itmobot.parallel

import java.util.concurrent.Executors

class ThreadHolder {
    companion object {
        // TODO add number of threads to config
        val scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())
    }
}