package ru.vladrus13.itmobot.database

import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.database.entities.UserParser
import java.lang.Boolean.TRUE
import java.util.logging.Logger

class DataBase {
    companion object {

        val logger: Logger = Logger.getLogger(DataBase::class.java.simpleName)

        fun getUsersWithNotification(): List<User> =
            DataBaseParser.get { UserParser.Companion.Users.notification eq TRUE }

        inline fun <reified T> put(chatId: Long, o: T) = DataBaseParser.set(chatId, o)

        inline fun <reified T> get(chatId: Long): T = DataBaseParser.get(chatId)

        @Throws(IllegalArgumentException::class)
        fun init() = DataBaseParser.init()
    }
}