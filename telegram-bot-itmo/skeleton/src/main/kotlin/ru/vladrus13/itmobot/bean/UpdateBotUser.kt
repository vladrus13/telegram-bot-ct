package ru.vladrus13.itmobot.bean

import ru.vladrus13.itmobot.updates.Updates

class UpdateBotUser(
    val chatId: Long,
    val version: String = Updates.getLast()
)