package ru.vladrus13.itmobot.utils

import org.apache.logging.log4j.kotlin.Logging

class SafeRunnable(val runnable: Runnable) : Runnable, Logging {
    override fun run() {
        try {
            runnable.run()
        } catch (e: Exception) {
            logger.warn("Something went wrong", e)
        }
    }
}