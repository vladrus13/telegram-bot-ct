package ru.vladrus13.itmobot.plugin.homework

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase

class RemoveTeamCommand : Menu(arrayOf()) {
    override val menuHelp = "Удаление команд"
    override val name = "Роспуск команды"

    override fun getAdditionalButtonsForReply(user: User): List<String> {
        return if (user.path.getData("selectedTeam") == null) {
            TeamRoleDatabase.getAllTeamsWhereUserIsAdmin(user.chatId)
        } else {
            val team = TeamDatabase.getById(user.path.getData("selectedTeam")!!.toLong())!!
            arrayListOf(team.name)
        }
    }

    override fun onCustomUpdate(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
        if (user.path.getData("selectedTeam") != null) {
            val team = TeamDatabase.getById(user.path.getData("selectedTeam")!!.toLong())!!
            if (update.message.text == team.name) {
                val users = TeamRoleDatabase.getByTeamId(team.id)
                TeamRoleDatabase.deleteByTeamId(team.id)
                TeamDatabase.deleteByTeamId(team.id)
                TeamTaskDatabase.deleteByTeamId(team.id)
                user.path.returnBack()
                user.send(
                    bot = bot,
                    text = "Успешно! Происходит отправка о роспуске всем участникам...",
                    replyKeyboard = user.path.last().getReplyKeyboard(user)
                )
                users.forEach { role ->
                    DataBase.get<User>(role.userId).send(
                        bot = bot,
                        text = "Ваша команда под названием \"${team.name}\" распущена!"
                    )
                }
            } else {
                user.path.returnBack()
                user.send(
                    bot = bot,
                    text = "Неверно!",
                    replyKeyboard = user.path.last().getReplyKeyboard(user)
                )
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
                user.path.setData("selectedTeam", team.id.toString())
                user.send(
                    bot = bot,
                    text = "ВНИМАНИЕ! Это действие удалит группу, всех ее участников из нее, и все домашние задания из нее. Данное действие отменить будет невозможно!"
                )
                user.send(
                    bot = bot,
                    text = "Если хотите продолжить, введите имя группы еще раз",
                    replyKeyboard = getReplyKeyboard(user)
                )
            }
        }
        return true
    }
}