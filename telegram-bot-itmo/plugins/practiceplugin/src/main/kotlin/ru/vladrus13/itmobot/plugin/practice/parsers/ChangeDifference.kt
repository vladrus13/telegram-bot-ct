package ru.vladrus13.itmobot.plugin.practice.parsers

interface ChangeDifference {
    suspend fun changeTable(id: String): Unit
}