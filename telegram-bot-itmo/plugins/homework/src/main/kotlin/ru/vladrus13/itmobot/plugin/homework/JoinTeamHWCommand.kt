package ru.vladrus13.itmobot.plugin.homework

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.utils.Utils

class JoinTeamHWCommand(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp(): String = "Позволяет вам зайти в команду"

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        val list : MutableList<KeyboardRow> = if (user.path.getData("teamId") == null) {
            val rolesOfUser = HomeworkPlugin.TeamRoleDatabase.getAllByFilter { HomeworkPlugin.TeamRoleTable.userId eq user.chatId }.map { it.teamId }
            Utils.splitBy(
                HomeworkPlugin.TeamDatabase.getAll().filter { !rolesOfUser.contains(it.id) }.map { it.name }
            )
        } else {
            mutableListOf()
        }
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        list.add(backRow)
        replyKeyboardMarkup.keyboard = list
        return replyKeyboardMarkup
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        if (user.path.getData("teamId") != null) {
            val teamId = user.path.getData("teamId")!!.toLong()
            val password = update.message.text
            val team = HomeworkPlugin.TeamDatabase.getById(teamId)
            if (team == null) {
                user.send(
                    bot = bot,
                    text = "НЕКОрректное имя группы!"
                )
            } else {
                if (team.password == password) {
                    HomeworkPlugin.TeamRoleDatabase.put(HomeworkPlugin.TeamRole(user.chatId, teamId, 1))
                    user.path.setPath(parent.path)
                    user.send(
                        bot = bot,
                        text = "Вы успешно вошли",
                        replyKeyboard = parent.getReplyKeyboard(user)
                    )
                } else {
                    user.path.setPath(parent.path)
                    user.send(
                        bot = bot,
                        text = "НЕКОрректный пароль!",
                        replyKeyboard = parent.getReplyKeyboard(user)
                    )
                }
            }
        } else {
            val teamName = update.message.text
            val team = HomeworkPlugin.TeamDatabase.getByName(teamName)
            if (team == null) {
                user.send(
                    bot = bot,
                    text = "НЕКОрректное имя группы"
                )
            } else {
                val role = HomeworkPlugin.TeamRoleDatabase.get(user.chatId, team.id)
                if (role != null) {
                    user.send(
                        bot = bot,
                        text = "Вы пытаетесь войти туда, где вы уже есть!"
                    )
                } else {
                    user.path.setData("teamId", team.id.toString())
                    user.send(
                        bot = bot,
                        text = "Введите пароль"
                    )
                }
            }
        }
    }

    override val name: String = "Присоединиться к команде"
    override val systemName: String = "joinHWTeam"

    override fun isAccept(update: Update): Boolean =
        update.message.text == name
}