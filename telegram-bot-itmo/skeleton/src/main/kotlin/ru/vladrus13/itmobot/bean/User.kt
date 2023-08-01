package ru.vladrus13.itmobot.bean

import ru.vladrus13.itmobot.database.DataBase

class User(
    chatId: Long,
    val role: Int = 0,
    group: String? = null,
    var name: String? = null,
    var username: String? = null,
    settings: Settings = Settings(3),
    var path: UserPath,
    var notification: Boolean = true,
    private var usersSubjects: UsersSubjects? = null,
    private var usersPlugins: UserPlugins? = null,
    updateBotUser: UpdateBotUser? = null
) : Chatted(chatId, group, settings, updateBotUser) {

    fun getSubjects(forced: Boolean = false): UsersSubjects {
        if (forced || usersSubjects == null) {
            usersSubjects = DataBase.get(chatId)
        }
        return usersSubjects!!
    }

    fun setSubjects() {
        DataBase.put(chatId, usersSubjects)
    }

    fun getPlugins(forced: Boolean = false): UserPlugins {
        if (forced || usersPlugins == null) {
            usersPlugins = DataBase.get(chatId)
        }
        return usersPlugins!!
    }

    fun setPlugins() {
        DataBase.put(chatId, usersPlugins)
    }

    override fun save() {
        DataBase.put(chatId, this)
    }
}