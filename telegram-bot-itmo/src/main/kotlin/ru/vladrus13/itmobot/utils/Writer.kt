package ru.vladrus13.itmobot.utils

import java.io.PrintWriter
import java.io.StringWriter
import java.util.logging.Logger

class Writer {
    companion object {
        fun printStackTrace(logger: Logger, e: Exception) {
            val stringWriter = StringWriter()
            e.printStackTrace(PrintWriter(stringWriter))
            logger.severe(stringWriter.toString())
        }
    }
}