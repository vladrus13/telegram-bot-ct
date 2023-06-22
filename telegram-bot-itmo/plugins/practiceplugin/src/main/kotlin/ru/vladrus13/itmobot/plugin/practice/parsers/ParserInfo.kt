package ru.vladrus13.itmobot.plugin.practice.parsers

import ru.vladrus13.itmobot.properties.InitialProperties
import java.util.logging.Logger

/**
 * This interface uses for developing parsing files, sites and another things for updating google table
 */
interface ParserInfo {
    val idTable: String
    val urlInfo: String

    /**
     * This method return is Changed info about tasks or not.
     */
    suspend fun isChanged(): Boolean

    /**
     * getDifference between tasks
     */
    suspend fun getDifference(): ChangeDifference
}