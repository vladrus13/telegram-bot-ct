package ru.vladrus13.itmobot.service

import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.database.DataBase

class UserService {
    companion object {
        fun get(chatId: Long): User = DataBase.get(chatId)

        fun set(user: User) = DataBase.put(user.chatId, user)
    }
}