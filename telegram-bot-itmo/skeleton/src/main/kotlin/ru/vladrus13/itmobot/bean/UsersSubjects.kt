package ru.vladrus13.itmobot.bean

class UsersSubjects(val chatId: Long) {

    val include: MutableList<String> = ArrayList()
    val exclude: MutableList<String> = ArrayList()

    constructor(chatId: Long, include: String, exclude: String) : this(chatId) {
        this.include.addAll(include.split("~~"))
        this.exclude.addAll(exclude.split("~~"))
    }

    fun getInclude(): String {
        return include.joinToString(separator = "~~")
    }

    fun getExclude(): String {
        return exclude.joinToString(separator = "~~")
    }
}