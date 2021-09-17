package ru.vladrus13.itmobot.exceptions

open class ItmoBotException(private val error: String) : Exception(error)