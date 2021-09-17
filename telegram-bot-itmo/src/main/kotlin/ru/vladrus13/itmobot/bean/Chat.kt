package ru.vladrus13.itmobot.bean

class Chat(
    chatId: Long,
    group: String? = null,
    var name: String? = null
) : Chatted(chatId, group)