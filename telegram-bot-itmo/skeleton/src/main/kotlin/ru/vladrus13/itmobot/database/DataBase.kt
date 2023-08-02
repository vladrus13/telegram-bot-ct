package ru.vladrus13.itmobot.database

import org.apache.logging.log4j.kotlin.Logging
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.database.entities.UserParser
import java.lang.Boolean.TRUE

class DataBase {
    companion object : Logging {
        fun getUsersWithNotification(): List<User> =
                DataBaseParser.get { UserParser.Users.notification eq TRUE }

        inline fun <reified T> put(chatId: Long, o: T) = DataBaseParser.set(chatId, o)

        inline fun <reified T> get(chatId: Long): T = DataBaseParser.get(chatId)

        inline fun <reified T> get(noinline filter: SqlExpressionBuilder.() -> Op<Boolean>): List<T> =
                DataBaseParser.get(filter)

        @Throws(IllegalArgumentException::class)
        fun init() = DataBaseParser.init()
    }
}