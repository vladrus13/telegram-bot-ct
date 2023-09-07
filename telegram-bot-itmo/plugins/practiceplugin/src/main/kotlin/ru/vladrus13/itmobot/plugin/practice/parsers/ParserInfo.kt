package ru.vladrus13.itmobot.plugin.practice.parsers

/**
 * This interface uses for developing parsing files, sites and another things for updating google table
 */
interface ParserInfo {
    val urlInfo: String

    /**
     * getDifference between tasks
     */
    fun getTasks(): List<String>
}