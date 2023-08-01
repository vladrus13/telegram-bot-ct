package ru.vladrus13.itmobot.plugin.homework

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.plugin.homework.TeamRoleDatabase.*
import ru.vladrus13.itmobot.utils.Utils

class JoinTeamCommand : Menu(arrayOf()) {
    override val menuHelp: String
        get() = "Позволяет вам зайти в команду"
    override val name: String
        get() = "Присоединиться к команде"

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val rows: MutableList<KeyboardRow> = if (user.path.getData("teamId") == null) {
            val teamsOfUser = TeamRoleDatabase.getAllTeamsForUser(user.chatId)
            Utils.splitBy(
                TeamDatabase.getAllTeamsExcept(teamsOfUser)
            )
        } else {
            mutableListOf()
        }
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        rows.add(backRow)
        return ReplyKeyboardMarkup(rows)
    }

    override fun onCustomUpdate(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
        if (user.path.getData("teamId") != null) {
            val teamId = user.path.getData("teamId")!!.toLong()
            val password = update.message.text
            val team = TeamDatabase.getById(teamId)
            if (team == null) {
                user.send(
                    bot = bot,
                    text = "НЕКОрректное имя группы!"
                )
            } else {
                if (team.password == password) {
                    TeamRoleDatabase.put(TeamRole(user.chatId, teamId, 1))
                    user.path.myRemoveFromPath()
                    user.send(
                        bot = bot,
                        text = "Вы успешно вошли",
                        replyKeyboard = user.path.myLast().getReplyKeyboard(user)
                    )
                } else {
                    user.path.myRemoveFromPath()
                    user.send(
                        bot = bot,
                        text = "НЕКОрректный пароль!",
                        replyKeyboard = user.path.myLast().getReplyKeyboard(user)
                    )
                }
            }
        } else {
            val teamName = update.message.text
            val team = TeamDatabase.getByName(teamName)
            if (team == null) {
                user.send(
                    bot = bot,
                    text = "НЕКОрректное имя группы"
                )
            } else {
                val role = TeamRoleDatabase.get(user.chatId, team.id)
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
        return true
    }
}