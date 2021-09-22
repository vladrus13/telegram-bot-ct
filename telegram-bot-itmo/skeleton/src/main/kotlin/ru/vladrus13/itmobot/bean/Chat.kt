package ru.vladrus13.itmobot.bean

import ru.vladrus13.itmobot.database.DataBase

class Chat(
    chatId: Long,
    group: String? = null,
    var name: String? = null
) : Chatted(chatId, group) {
    override fun save() {
        DataBase.put(chatId, this)
    }
}