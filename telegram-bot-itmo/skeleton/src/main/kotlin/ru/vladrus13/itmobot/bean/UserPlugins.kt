package ru.vladrus13.itmobot.bean

class UserPlugins(val chatId: Long) {

    val plugins: MutableList<String> = ArrayList()

    constructor(chatId: Long, include: String) : this(chatId) {
        this.plugins.addAll(include.split("~~"))
    }

    fun getPlugins(): String {
        return plugins.joinToString(separator = "~~")
    }
}