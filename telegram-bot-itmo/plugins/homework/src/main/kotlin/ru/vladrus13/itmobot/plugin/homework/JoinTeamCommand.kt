package ru.vladrus13.itmobot.plugin.homework

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.plugin.homework.TeamRoleDatabase.TeamRole

class JoinTeamCommand : Menu(arrayOf()) {
    override val menuHelp = "Позволяет вам зайти в команду"
    override val name = "Присоединиться к команде"

    override fun getAdditionalButtonsForReply(user: User): List<String> {
        return if (user.path.getData("teamId") == null) {
            val teamsOfUser = TeamRoleDatabase.getAllTeamsForUser(user.chatId)
            TeamDatabase.getAllTeamsExcept(teamsOfUser)
        } else {
            mutableListOf()
        }
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
                    user.path.returnBack()
                    user.send(
                        bot = bot,
                        text = "Вы успешно вошли",
                        replyKeyboard = user.path.last().getReplyKeyboard(user)
                    )
                } else {
                    user.path.returnBack()
                    user.send(
                        bot = bot,
                        text = "НЕКОрректный пароль!",
                        replyKeyboard = user.path.last().getReplyKeyboard(user)
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